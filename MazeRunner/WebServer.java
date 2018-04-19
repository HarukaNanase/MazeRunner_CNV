import java.io.*;
import java.net.InetSocketAddress;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
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
    public static void main(String[] args) throws Exception {
        Path currentRelativePath = Paths.get("");
        CURRENT_PATH = currentRelativePath.toAbsolutePath().toString();
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/mzrun.html", new MazeHandler());
        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool()); // creates a default executor
        DynamoController.init();
        System.out.println("WebServer: Ready to receive mazes.");
        server.start();
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
        }
        static synchronized void SaveMetrics(){
            try{
                MetricsData metricsThread = WebServer.getHashMap().get(Thread.currentThread().getId());
                DynamoController.init();
                if(mapper == null)
                    mapper = new DynamoDBMapper(DynamoController.dynamoDB);
                mapper.save(metricsThread);
            }catch(Exception e){
                System.out.println(e.getMessage());
            }
            MetricsData metricsThread = WebServer.getHashMap().get(Thread.currentThread().getId());
            System.out.println("Thread " + metricsThread.getThreadId() + " Metrics Data:");
            System.out.println("Request for this thread: " + metricsThread.getRequestQuery());
            System.out.println("Instructions Run: " + metricsThread.getInstructionsRun());
            System.out.println("BasicBlocks Found: " + metricsThread.getBasicBlocksFound());
            System.out.println("Methods Count: " + metricsThread.getMethodsCount());
            System.out.println("Memory Allocs: " + metricsThread.getMemoryCalls());
            System.out.println("Strategy Runs: " + metricsThread.getLoopRuns());
        }
    }


        static byte[] SolveMaze(String args){
        try{
            String[] final_args = GetQueryValues(args);
            System.out.println("Solving maze...");
            String path = CURRENT_PATH + "/" + final_args[7];
            thread_requests.get(Thread.currentThread().getId()).setUUID(final_args[7].substring(final_args[7].lastIndexOf("/")+1));
            Main.main(final_args);

            return Files.readAllBytes(Paths.get(path));
        }catch(Exception e){
            return e.getMessage().getBytes();
        }
    }

    static String[] GetQueryValues(String args){
        String[] args_array = args.split("&");
        ArrayList<String> list = new ArrayList<String>(Arrays.asList(args_array));
        list.add(list.get(0));
        list.remove(0);
        for(int i = 0; i < list.size(); i++)
            list.set(i,GetKeyValue(list.get(i)));
        list.add(GenerateRandomFileName());
        return list.toArray(new String[list.size()]);
    }

    static String GenerateRandomFileName(){
        return SOLUTIONS_PATH + UUID.randomUUID().toString();
    }

    static String GetKeyValue(String key_value){
        return key_value.substring(key_value.lastIndexOf("=")+1);
    }

    static HashMap<Long, MetricsData> getHashMap(){
        return thread_requests;
    }
}
