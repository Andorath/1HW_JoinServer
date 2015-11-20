package joinserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Damiano Di Stefano, Marco Giuseppe Salafia
 */
public class JoinServer
{
    public static final int serverPort = 9999;
    public static final int nThread = 100;
    
    private volatile TreeMap<InetSocketAddress, HashSet<InetSocketAddress>> networkMap;
    ServerSocket server;

    public JoinServer()
    {
        this.networkMap = new TreeMap<>();
    }
    
    public void start()
    {
        try
        {
            server = new ServerSocket(serverPort);
            System.out.println("SERVER AVVIATO!");
            
            Executor executor = Executors.newFixedThreadPool(nThread);
            
            while(true)
            {
                Socket clientSocket = server.accept();
                JSHandler worker = new JSHandler(clientSocket, networkMap);
                executor.execute(worker);
            }
            
        }
        catch (IOException ex)
        {
            Logger.getLogger(JoinServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
