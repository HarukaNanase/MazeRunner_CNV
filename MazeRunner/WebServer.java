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
public class WebServer {

    private static String CURRENT_PATH;
    public static void main(String[] args) throws Exception {
        Path currentRelativePath = Paths.get("");
        CURRENT_PATH = currentRelativePath.toAbsolutePath().toString();
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/mzrun.html", new MyHandler());
        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool()); // creates a default executor
        server.start();
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            //String response = "This was the query:" + t.getRequestURI().getQuery() + "##";
            String response = SolveMaze(t.getRequestURI().getQuery());
            //t.getRequestHeaders().add("Content-Type", "text/html");
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    public static String SolveMaze(String args){
        try{
            String[] final_args = GetQueryValues(args);
            Main.main(final_args);
            StringBuilder sb = new StringBuilder();
            String path = CURRENT_PATH + "/" + final_args[7];
            try (BufferedReader r = Files.newBufferedReader(Paths.get(path), Charset.forName("UTF-8"))) {
                String s;
                while((s = r.readLine()) != null)
                    sb.append(s);
            }
            return sb.toString();


        }catch(Exception e){
            return "Error";
        }
    }

    public static String[] GetQueryValues(String args){
        String[] args_array = args.split("&");
        ArrayList<String> list = new ArrayList<String>(Arrays.asList(args_array));
        list.add(list.get(0));
        list.remove(0);
        String fileName = "GenerateRandomName.html";
        list.add(fileName);
        for(int i = 0; i < list.size(); i++){
            String s = list.get(i);
            list.set(i,s.substring(s.lastIndexOf("=")+1));
        }
        return list.toArray(new String[list.size()]);
    }


}
