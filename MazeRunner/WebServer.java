import java.io.*;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.*;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class WebServer {

    private static String CURRENT_PATH;
    public static void main(String[] args) throws Exception {
        Path currentRelativePath = Paths.get("");
        CURRENT_PATH = currentRelativePath.toAbsolutePath().toString();
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/mzrun.html", new MazeHandler());
        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool()); // creates a default executor
        server.start();
    }

    static class MazeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            byte[] response = SolveMaze(t.getRequestURI().getQuery());
            t.sendResponseHeaders(200, response.length);
            OutputStream os = t.getResponseBody();
            os.write(response);
            os.close();
        }
    }

    public static byte[] SolveMaze(String args){
        try{
            String[] final_args = GetQueryValues(args);
            System.out.println("Solving maze...");
            Main.main(final_args);
            StringBuilder sb = new StringBuilder();
            String path = CURRENT_PATH + "/" + final_args[7];
            return Files.readAllBytes(Paths.get(path));

        }catch(Exception e){
            return "Something went terribly wrong.".getBytes();
        }
    }

    public static String[] GetQueryValues(String args){
        String[] args_array = args.split("&");
        ArrayList<String> list = new ArrayList<String>(Arrays.asList(args_array));
        list.add(list.get(0));
        list.remove(0);
        String fileName = "solutions/"+UUID.randomUUID().toString();
        for(int i = 0; i < list.size(); i++){
            String s = list.get(i);
            list.set(i,s.substring(s.lastIndexOf("=")+1));
        }
        list.add(fileName);
        return list.toArray(new String[list.size()]);
    }


}
