import java.io.*;
import java.net.InetSocketAddress;

import Heuristics.RequestHeuristic;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.http.client.methods.RequestBuilder;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import com.google.gson.Gson;
public class WebServer {

    private static String CURRENT_PATH;
    private static String SOLUTIONS_PATH = "solutions/";
    private static HashMap<Long, MetricsData> thread_requests = new HashMap<>();
    private static DynamoDBMapper mapper;
    private static int RequestsBeingSolved = 0;
    public static void main(String[] args) throws Exception {
        Path currentRelativePath = Paths.get("");
        CURRENT_PATH = currentRelativePath.toAbsolutePath().toString();
        HttpServer server = HttpServer.create(new InetSocketAddress(80), 0);
        server.createContext("/mzrun.html", new MazeHandler());
        server.createContext("/alive", new AliveHandler());
        server.createContext("/jobstate", new JobIdHandler());
        server.createContext("/mazecount", new MazeCounterHandler());
        server.createContext("/currentworkload", new WorkloadHandler());
        
        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool()); // creates a default executor
        DynamoController.init();
        mapper = new DynamoDBMapper(DynamoController.dynamoDB);
        System.out.println("WebServer: Ready to receive mazes.");
        server.start();
    }


    static class MazeCounterHandler implements HttpHandler{
        @Override
        public void handle(HttpExchange t) throws IOException{
            t.sendResponseHeaders(200, (""+RequestsBeingSolved).getBytes().length);
            OutputStream os = t.getResponseBody();
            os.write((""+ RequestsBeingSolved).getBytes());
            os.close();
        }
    }

    static class AliveHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "Ola";
            t.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    static class WorkloadHandler implements HttpHandler{
        @Override
        public void handle(HttpExchange t) throws IOException {
            int absoluteWorkload = 0;
            for (Map.Entry<Long, MetricsData> entry : thread_requests.entrySet()) {
                absoluteWorkload += entry.getValue().getWorkload(new RequestHeuristic());
            }

            t.sendResponseHeaders(200, (""+absoluteWorkload).getBytes().length);
            OutputStream os = t.getResponseBody();
            os.write((""+absoluteWorkload).getBytes());
            os.close();
        }
    }

    static class MazeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException{
            try {
                thread_requests.put(Thread.currentThread().getId(), new MetricsData(Thread.currentThread().getId(), t.getRequestURI().getQuery()));
                byte[] response = SolveMaze(t.getRequestURI().getQuery());
                t.sendResponseHeaders(200, response.length);
                OutputStream os = t.getResponseBody();
                os.write(response);
                os.close();
		        if(response.length > 50)
                	SaveMetrics();
                DecrementRequests();
                thread_requests.remove(Thread.currentThread().getId());
            }catch(Exception io){
                t.sendResponseHeaders(200, io.getMessage().getBytes().length);
                OutputStream os = t.getResponseBody();
                os.write(io.getMessage().getBytes());
                os.close();
            }
            //System.out.println("Total branches taken: " + thread_requests.get(Thread.currentThread().getId()).getBranches_taken());
        }
      }
      static class JobIdHandler implements HttpHandler {
        @Override
          public void handle(HttpExchange t) throws IOException{
            String query = t.getRequestURI().getQuery();
            System.out.println("Query: " + query);
            String job_id = query.substring(query.lastIndexOf("&")+1);
            int id = Integer.parseInt(job_id.split("=")[1]);
            System.out.println("Fetching data for jobid " + id);
            MetricsData current = null;

            for(Map.Entry<Long, MetricsData> entry : thread_requests.entrySet()){
                if(entry.getValue().getJobId() == id)
                    current = entry.getValue();
            }
            if(current == null){
                t.sendResponseHeaders(200, "Failed to fetch jobid".getBytes().length);
                OutputStream os = t.getResponseBody();
                os.write("Failed to fetch jobid".getBytes());
                os.close();
                return;
            }
            Gson gson = new Gson();
            String data = gson.toJson(current);
            t.sendResponseHeaders(200, data.getBytes().length);
            OutputStream os = t.getResponseBody();
            os.write(data.getBytes());
            os.close();
        }
      }

      public static synchronized void DecrementRequests(){
        RequestsBeingSolved--;
      }

      public static synchronized void IncrementRequests(){
          RequestsBeingSolved++;
      }

      static synchronized void SaveMetrics(){
            try{
                MetricsData metricsThread = WebServer.getHashMap().get(Thread.currentThread().getId());
                mapper.save(metricsThread);
            }catch(Exception e){
                System.out.println(e.getMessage());
            }
            MetricsData metricsThread = WebServer.getHashMap().get(Thread.currentThread().getId());
            System.out.println("Thread " + metricsThread.getThreadId() + " Metrics Data:");
            System.out.println("Request for this thread: " + metricsThread.getRequestQuery());
            System.out.println("BasicBlocks Found: " + (metricsThread.getBasicBlocksFound() + metricsThread.getObserveBB()));
            System.out.println("Methods Count: " + metricsThread.getMethodsCount());
            System.out.println("Memory Allocs: " + metricsThread.getMemoryCalls());
            System.out.println("Strategy Runs: " + metricsThread.getLoopRuns());
            System.out.println("Strategy Observes: " + metricsThread.getLoopObserves());
            //System.out.println("Estimated BB for Run: " + metricsThread.getEstimatedRunBBL());
            //System.out.println("Estimated BB for Observe: " + metricsThread.getEstimatedObserveBBL());
            System.out.println("Branches taken: " + metricsThread.getBranches_taken());
	    System.out.println("x0: " + metricsThread.getX0());
		System.out.println("y0: " + metricsThread.getY0());
		System.out.println("x1: " + metricsThread.getX1());
		System.out.println("y1: " + metricsThread.getY1());

      }


    static synchronized void GetMetricsByStrategy(String strategyName){
      MetricsData data = new MetricsData();
    }


        static byte[] SolveMaze(String args){
        try{
            HashMap<String,String> final_args = GetQueryValues(args);
            System.out.println("Solving maze..." + args);
            String path = CURRENT_PATH + "/" + final_args.get("filename");
            thread_requests.get(Thread.currentThread().getId()).setUUID(final_args.get("filename").substring(final_args.get("filename").lastIndexOf("/")+1));
            thread_requests.get(Thread.currentThread().getId()).setVelocity(Integer.parseInt(final_args.get("v")));
            thread_requests.get(Thread.currentThread().getId()).setMazeType(final_args.get("m"));
            thread_requests.get(Thread.currentThread().getId()).setX0(Integer.parseInt(final_args.get("x0")));
            thread_requests.get(Thread.currentThread().getId()).setX1(Integer.parseInt(final_args.get("x1")));
            thread_requests.get(Thread.currentThread().getId()).setY0(Integer.parseInt(final_args.get("y0")));
            thread_requests.get(Thread.currentThread().getId()).setY1(Integer.parseInt(final_args.get("y1")));
            thread_requests.get(Thread.currentThread().getId()).setJobId(Integer.parseInt(final_args.get("jobid")));
            thread_requests.get(Thread.currentThread().getId()).setStrategy(final_args.get("s"));
            thread_requests.get(Thread.currentThread().getId()).setExpectedBranches(Long.parseLong(final_args.get("expectedBranches")));
            thread_requests.get(Thread.currentThread().getId()).setExpectedWorkload(Double.parseDouble(final_args.get("expectedWorkload")));
            IncrementRequests();
            Main.main(buildParameterArray(final_args));
            return Files.readAllBytes(Paths.get(path));
        }catch(Exception e){
            return e.getMessage().getBytes();
        }
    }

    //m=maze100.maze&x0=3&y0=9&x1=78&y1=89&v=50&s=astar
    static String[] buildParameterArray(HashMap<String, String> params){
       return new String[]{params.get("x0"), params.get("y0"), params.get("x1"), params.get("y1"), params.get("v"), params.get("s"),params.get("m") ,params.get("filename")};
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
        params.put("filename", GenerateRandomFileName());
        return params;
    }

    static String GenerateRandomFileName(){
        return SOLUTIONS_PATH + UUID.randomUUID().toString();
    }

    static String GetValue(String key_value){
        return key_value.substring(key_value.lastIndexOf("=")+1);
    }
    static String GetKey(String key_value) { return key_value.split("=")[0]; }
    static HashMap<Long, MetricsData> getHashMap(){
        return thread_requests;
    }
}
