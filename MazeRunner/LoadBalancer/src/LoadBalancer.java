import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.xray.model.Http;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LoadBalancer {
    private static EC2AutoScaler scaler = null;
    private static DynamoDBMapper mapper = null;
    public static void main(String[] args) throws Exception{
        scaler = new EC2AutoScaler();
        DynamoController.init();
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/alive", new AliveHandler());
        server.createContext("/launchmachine", new LauncherHandler());
        server.createContext("/mzrun.html", new LoadBalancerHandler());
        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool()); // creates a default executor
        System.out.println("LoadBalancer: Ready to receive mazes.");
        server.start();
    }



    static class LoadBalancerHandler implements HttpHandler{
        @Override
        public void handle(HttpExchange t) throws IOException{
            EC2Machine bestMachine = scaler.getBestMachine();
            String query = t.getRequestURI().getQuery();
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
            scaler.createNewMachine();
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

    public static List<MetricsData> getSimilarMazes(String mazetype){
        if(mapper == null)
            mapper = new DynamoDBMapper(DynamoController.dynamoDB);

        MetricsData ex = new MetricsData();
        ex.setMazeType(mazetype);
        DynamoDBQueryExpression<MetricsData> query_map = new DynamoDBQueryExpression<MetricsData>();
        query_map.setHashKeyValues(ex);

        return mapper.query(MetricsData.class, query_map);
    }

}
