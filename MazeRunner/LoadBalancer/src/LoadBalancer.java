import Heuristics.RequestHeuristic;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.xspec.N;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.*;

public class LoadBalancer {
    private static EC2AutoScaler scaler = null;
    private static DynamoDBMapper mapper = null;
    private static int MAXIMUM_HEAVYNESS = 85;
    private static int currentJobId = 0;
    private static String REGION = "us-east-1";
    private static String IMAGE_ID = "ami-065bbcea27d201037";
    private static String INSTANCE_TYPE = "t2.micro";
    private static String KEY_NAME = "CNV_AWS";
    private static String SECURITY_GROUP = "CNV-HTTP-SSH";
    private static int WORKLOAD_THRESHOLD = 90;
    private static ArrayList<String> queries = new ArrayList<String>();
   // private static HashMap<Pair<String, String>, > cache;
    public static void main(String[] args) throws Exception{
        scaler = new EC2AutoScaler();
        TimerTask scalerTask = new TimerTask() {
            @Override
            public void run() {
                scaler.checkSystemState();
            }
        };
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(scalerTask, 0, scaler.getSystemCheckTime() * 1000);
        DynamoController.init();
        HttpServer server = HttpServer.create(new InetSocketAddress(80), 0);
        server.createContext("/alive", new AliveHandler());
        server.createContext("/launchmachine", new LauncherHandler());
        server.createContext("/shutdown", new ShutdownHandler());
        server.createContext("/mzrun.html", new LoadBalancerHandler());
        server.createContext("/requestcount", new RequestCountHandler());
        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool()); // creates a default executor
        System.out.println("LoadBalancer: Ready to receive mazes.");
        server.start();
    }


    static class RequestCountHandler implements HttpHandler{
        @Override
        public void handle(HttpExchange t) throws IOException{
            EC2Machine ec2 = scaler.getBestMachine(0);
            int requestCount = ec2.getServerRequestCount();
            t.sendResponseHeaders(200, ("RequestCount: " + requestCount).getBytes().length);
            OutputStream os = t.getResponseBody();
            os.write(("RequestCount: " + requestCount).getBytes());
            os.close();
        }
    }


    static class ShutdownHandler implements HttpHandler{
        @Override
        public void handle(HttpExchange t) throws IOException{
            scaler.terminateAllMachines();
            System.out.println("System shutting down...");
            t.sendResponseHeaders(200, "Shutdown done.".getBytes().length);
            OutputStream os = t.getResponseBody();
            os.write("Shutdown done.".getBytes());
            os.close();
        }
    }


    static class LoadBalancerHandler implements HttpHandler{
        @Override
        public void handle(HttpExchange t) throws IOException{
            String similar = getSimilarWorkloadValue(t.getRequestURI().getQuery());
            System.out.println("Similar: " + similar);
            String query = t.getRequestURI().getQuery()+similar+"&jobid="+(++currentJobId);
            Map<String, String> values = GetQueryValues(query);
            int workload = Integer.parseInt(values.get("expectedWorkload"));
            EC2Machine bestMachine = scaler.getBestMachine(workload);
            if(bestMachine != null){
                System.out.println("Dispatching request to: " + bestMachine.getPublicDNS());
                byte[] solvedMaze = bestMachine.getMazeSolution(query);
                t.sendResponseHeaders(200, solvedMaze.length);
                OutputStream os = t.getResponseBody();
                os.write(solvedMaze);
                os.close();

            }else{
                String response = "No available servers right now.";
                t.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }

    static class LauncherHandler implements HttpHandler{
        @Override
        public void handle(HttpExchange t) throws IOException{
            scaler.createNewMachine(IMAGE_ID, INSTANCE_TYPE, KEY_NAME, SECURITY_GROUP, REGION);
            t.sendResponseHeaders(200, "Machine ran sucessfully".getBytes().length);
            OutputStream os = t.getResponseBody();
            os.write("Machine ran sucessfully".getBytes());
            os.close();
        }
    }


    static class AliveHandler implements HttpHandler{
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "This is quite a nice tea!\n" + scaler;
            t.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }


    public int calculateRequestWorkload(String query){
        //TO DO: return workload for a specific request from 1 to 20.
        return 1;
    }

    public static int getQuadrantSize(String mazetype){
        if(mazetype.equals("Maze100.maze"))
            return 10;
        else if(mazetype.equals("Maze250.maze"))
            return 20;
        else if(mazetype.equals("Maze500.maze"))
            return 30;
        else if(mazetype.equals("Maze750.maze"))
            return 40;
        else if(mazetype.equals("Maze1000.maze"))
            return 50;
        else return 0;
    }


    public synchronized static List<MetricsData> getSimilarMazes(String mazetype, int x0, int y0, int x1, int y1, String strategy){
        if(mapper == null)
            mapper = new DynamoDBMapper(DynamoController.dynamoDB);
        System.out.println("Searching similar mazes");
        //calculate quadrant size depending on maze type

        int quadrant = getQuadrantSize(mazetype);


        HashMap<String, AttributeValue> attributeValues = new HashMap<String, AttributeValue>();
        attributeValues.put(":x0_minus", new AttributeValue().withN(""+(x0-quadrant)));
        attributeValues.put(":x0_plus", new AttributeValue().withN(""+(x0+quadrant)));
        attributeValues.put(":y0_minus", new AttributeValue().withN(""+(y0-quadrant)));
        attributeValues.put(":y0_plus", new AttributeValue().withN(""+(y0+quadrant)));
        attributeValues.put(":strategy", new AttributeValue().withS(strategy));
        MetricsData ex = new MetricsData();
        ex.setMazeType(mazetype);
        DynamoDBQueryExpression<MetricsData> query_map = new DynamoDBQueryExpression<MetricsData>()
            .withFilterExpression("x0 BETWEEN :x0_minus and :x0_plus AND y0 BETWEEN :y0_minus and :y0_plus AND strategy = :strategy")
            .withExpressionAttributeValues(attributeValues);
        query_map.setHashKeyValues(ex);
        return mapper.query(MetricsData.class, query_map);
    }

    public static EC2Machine getBestMachine(int workload){
        return scaler.getBestMachine(workload);
    }

    public synchronized MetricsData getCurrentStateByJobId(EC2Machine ec2, int jobid){
        HttpURLConnection conn = null;
        try{
            URL url = new URL("http://"+ec2.getPublicDNS()+"/jobstate?jobid="+jobid);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int contentLength = conn.getContentLength();
            Scanner scanner = new Scanner(conn.getInputStream());
            String response = scanner.useDelimiter("\\Z").next();
            scanner.close();
            return new Gson().fromJson(response, MetricsData.class);
        }catch(Exception e){
            return null;
        }finally{
            if(conn != null)
                conn.disconnect();
        }
    }

    public synchronized static String getSimilarWorkloadValue(String query){
        HashMap<String, String> values = GetQueryValues(query);
        String MazeType = values.get("m");
        int x0 = Integer.parseInt(values.get("x0"));
        int y0 = Integer.parseInt(values.get("y0"));
        int x1 = Integer.parseInt(values.get("x1"));
        int y1 = Integer.parseInt(values.get("y1"));
        String strategy = values.get("s");
        MetricsData thisRequest = new MetricsData();
        thisRequest.setMazeType(MazeType);
        thisRequest.setX0(x0);
        thisRequest.setY0(y0);
        thisRequest.setX1(x1);
        thisRequest.setY1(y1);
        thisRequest.setStrategy(strategy);
        List<MetricsData> dynamo_values = getSimilarMazes(MazeType,x0,y0,x1,y1,strategy);

        double median_workload = 0;
        long branches_taken = 0;
        for(MetricsData metrics : dynamo_values){
            if(metrics.equals(thisRequest)) {
                System.out.println("Heuristic Value for equal request: " + metrics.getWorkload(new RequestHeuristic()));
                return "&expectedWorkload="+Math.round(metrics.getWorkload(new RequestHeuristic()))+"&expectedBranches="+metrics.getBranches_taken();
            }
            median_workload+=metrics.getWorkload(new RequestHeuristic());
            branches_taken+=metrics.getBranches_taken();

        }
        if(dynamo_values.size() > 0){
            System.out.println("Found Similar Mazes: " + dynamo_values.size() + " Mazes");
            median_workload /= dynamo_values.size();
            branches_taken /= dynamo_values.size();
        }
        System.out.println("Median workload for this request: " + median_workload);
        return "&expectedWorkload=" + Math.round(median_workload) + "&expectedBranches="+branches_taken;


    }

    static HashMap<String, String> GetQueryValues(String args){
        HashMap<String, String> params = new HashMap<String,String>();
        String[] args_array = args.split("&");
        ArrayList<String> list = new ArrayList<String>(Arrays.asList(args_array));
        list.add(list.get(0));
        list.remove(0);
        for(String s : list) {
            params.put(GetKey(s), GetValue(s));
        }
        return params;
    }

    static String GetValue(String key_value){
        return key_value.substring(key_value.lastIndexOf("=")+1);
    }
    static String GetKey(String key_value) { return key_value.split("=")[0]; }


}
