package joinserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Comparator;
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
    public static final int SERVER_PORT = 9999;
    
    private static final int nThread = 100;
    
    private volatile TreeMap<InetSocketAddress, HashSet<InetSocketAddress>> networkMap;
    ServerSocket server;

    public JoinServer()
    {
        this.networkMap = new TreeMap<>(new Comparator<InetSocketAddress>()
        {
            @Override
            public int compare(InetSocketAddress o1, InetSocketAddress o2)
            {                
                if(!o1.getHostString().equalsIgnoreCase(o2.getHostString()))
                {
                    return o1.getHostString().compareToIgnoreCase(o2.getHostString());
                }
                else
                {
                    return Integer.compare(o1.getPort(), o2.getPort());
                }
            }
        });
    }
    
    public void start()
    {
        try
        {
            server = new ServerSocket(SERVER_PORT);
            System.out.println("SERVER AVVIATO!");
            
            Executor executor = Executors.newFixedThreadPool(nThread);
            
            while(true)
            {
                Socket clientSocket = server.accept();
                System.out.println("Connesso a: " + clientSocket.getInetAddress());
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
