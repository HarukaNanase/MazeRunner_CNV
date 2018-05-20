import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

public class HTTPRequest {

    public static byte[] GETRequest(String endpoint){
        HttpURLConnection conn = null;
        DataInputStream rd = null;
        try{
            URL url = new URL(endpoint);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            System.out.println("Content-Length: " + conn.getContentLength());
            if(conn.getContentLength() == -1){
                System.out.println("Failed to get maze solution. Retrying...");
                return GETRequest(endpoint);
            }
            byte[] response = new byte[conn.getContentLength()];
            rd = (new DataInputStream(conn.getInputStream()));
            int len = 0;
            while(len < response.length)
                len += rd.read(response, len, response.length - len);
            System.out.println("Got: "+len+" bytes.");
            return response;

        }catch(Exception e){
            e.printStackTrace();
            return null;
        }finally{
            if(conn != null)
                conn.disconnect();
            if(rd != null)
                try{
                    rd.close();}
                catch(IOException ie){
                    //
                }
        }
    }

    public static String GETRequestAsString(String endpoint){
        HttpURLConnection conn = null;
        try {
            //Create connection
           // System.out.println("Contacting: " + endpoint);
            URL url = new URL(endpoint);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(2000);
            conn.setRequestMethod("GET");
            if (conn.getContentLength() == -1)
                return null;
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }catch(SocketTimeoutException ste){
            return null;
        }catch(ConnectException ce){
            //System.out.println("Failed to connect to " + endpoint);
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

    }
}

