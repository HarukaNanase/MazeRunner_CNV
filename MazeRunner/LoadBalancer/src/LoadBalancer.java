import com.amazonaws.services.xray.model.Http;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;

public class LoadBalancer {
    static ArrayList<EC2Machine> instances = new ArrayList<EC2Machine>();

    public static void main(String[] args) throws Exception{
        HttpServer server = HttpServer.create(new InetSocketAddress(8001), 0);
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
            EC2Machine bestMachine = getLessOccupiedMachine();
            String query = t.getRequestURI().getQuery();
            if(bestMachine != null){
                System.out.println("Redirecting request to: " + bestMachine.getPublicDNS());
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
            EC2Machine machine = new EC2Machine();
            try{
                machine.launchMachine();
                instances.add(machine);
            }catch(Exception e){
                e.printStackTrace();
            }
            t.sendResponseHeaders(200, "Machine ran sucessfully".getBytes().length);
            OutputStream os = t.getResponseBody();
            os.write("Machine ran sucessfully".getBytes());
            os.close();
        }
    }


    static class AliveHandler implements HttpHandler{
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "This is quite a nice tea!";
            t.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    public static EC2Machine getLessOccupiedMachine(){
        EC2Machine bestMachine = null;
        int minimumRequestCount = -1;
        for(EC2Machine machine : instances){
            if(bestMachine == null || machine.getServerRequestCount() < minimumRequestCount){
                bestMachine = machine;
                minimumRequestCount = machine.getServerRequestCount();
            }
        }
        return bestMachine;
    }


}
