package TwitterApp;
import java.io.*;
import java.net.*;

public class Server {
    //static Vector<Client> clients = new Vector<>();
    static final int serverPort = 12345;

    static boolean shouldRun = true;



    public static void main(String[] args) throws IOException
    {
        //JSONObject jsonObject = new JSONObject();
        ServerSocket ss = new ServerSocket(serverPort);
        System.out.println("Server Started\n");

        int number =0;
        while(shouldRun)
        {
            Socket s = ss.accept();
            System.out.println("Client Connected\n");
            number++;
            new ClientHandler(s,"client"+number).start();//creates new client handler when a client connects


        }
    }
}
