import java.io.*;
import java.net.InetSocketAddress;

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

public class WebServer {

    private static String CURRENT_PATH;
    private static String SOLUTIONS_PATH = "solutions/";
    private static HashMap<Long, MetricsData> thread_requests = new HashMap<>();
    private static DynamoDBMapper mapper;
    private static int RequestsBeingSolved = 0;
    public static void main(String[] args) throws Exception {
        Path currentRelativePath = Paths.get("");
        CURRENT_PATH = currentRelativePath.toAbsolutePath().toString();
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/mzrun.html", new MazeHandler());
        server.createContext("/alive", new AliveHandler());
        server.createContext("/mazecount", new MazeCounterHandler());
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

    static class MazeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            thread_requests.put(Thread.currentThread().getId(), new MetricsData(Thread.currentThread().getId(), t.getRequestURI().getQuery()));
            byte[] response = SolveMaze(t.getRequestURI().getQuery());
            t.sendResponseHeaders(200, response.length);
            OutputStream os = t.getResponseBody();
            os.write(response);
            os.close();
            SaveMetrics();
            DecrementRequests();
            //System.out.println("Total branches taken: " + thread_requests.get(Thread.currentThread().getId()).getBranches_taken());
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


      }


    static synchronized void GetMetricsByStrategy(String strategyName){
      MetricsData data = new MetricsData();
    }


        static byte[] SolveMaze(String args){
        try{
            HashMap<String,String> final_args = GetQueryValues(args);
            System.out.println("Solving maze...");
            String path = CURRENT_PATH + "/" + final_args.get("filename");
            thread_requests.get(Thread.currentThread().getId()).setUUID(final_args.get("filename").substring(final_args.get("filename").lastIndexOf("/")+1));
            thread_requests.get(Thread.currentThread().getId()).setVelocity(Integer.parseInt(final_args.get("v")));
            thread_requests.get(Thread.currentThread().getId()).setMazeType(final_args.get("m"));
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
