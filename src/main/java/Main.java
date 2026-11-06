import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import static utils.Constants.*;

public class Main {
  public static void main(String[] args){
    System.out.println("Logs from your program will appear here!");
        ServerSocket serverSocket = null;
        Socket clientSocket = null;

        try {
          serverSocket = new ServerSocket(DEFAULT_PORT);
          serverSocket.setReuseAddress(true);
          clientSocket = serverSocket.accept();

          InputStream in = clientSocket.getInputStream();
          OutputStream out = clientSocket.getOutputStream();

          BufferedReader reader = new BufferedReader(new InputStreamReader(in));
          String line;

          while ((line = reader.readLine()) != null) {
              if (line.equalsIgnoreCase(PING)) {
                  out.write(PONG_RESPONSE.getBytes());
                  out.flush();
              }
          }

        } catch (IOException e) {
          System.out.println("IOException: " + e.getMessage());
        } finally {
          try {
            if (clientSocket != null) {
              clientSocket.close();
            }
          } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
          }
        }
  }
}
