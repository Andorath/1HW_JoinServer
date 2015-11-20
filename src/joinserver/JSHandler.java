package joinserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Damiano Di Stefano, Marco Giuseppe Salafia
 */
class JSHandler implements Runnable
{
    private volatile TreeMap<InetSocketAddress, HashSet<InetSocketAddress>> networkMap;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Socket clientSocket;

    public JSHandler(Socket clientSocket, TreeMap<InetSocketAddress, HashSet<InetSocketAddress>> networkMap)
    {
        try
        {
            this.networkMap = networkMap;
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());
        }
        catch (IOException ex)
        {
            Logger.getLogger(JSHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void run()
    {
        int peerPort = clientSocket.getPort();
        InetAddress peerInetAddress = clientSocket.getInetAddress();
        InetSocketAddress peerInetSocketAddress = new InetSocketAddress(peerInetAddress, peerPort);
        addPeer(peerInetSocketAddress);
        
    }
    
    private synchronized void addPeer(InetSocketAddress peerSocketAddress)
    {         
         if (!networkMap.containsKey(peerSocketAddress))
         {
             HashSet<InetSocketAddress> neighbours = new HashSet<>();
             
             neighbours.addAll(networkMap.keySet());
             updateOtherNeighboursWith(peerSocketAddress);
             
             networkMap.put(peerSocketAddress, neighbours);
             
             sendNetworkToAll();
         }
    }

    private synchronized void updateOtherNeighboursWith(InetSocketAddress peerSocketAddress)
    {
        for (Map.Entry<InetSocketAddress, HashSet<InetSocketAddress>> entry : networkMap.entrySet())
        {
            HashSet<InetSocketAddress> entryNeighbours = entry.getValue();
            entryNeighbours.add(peerSocketAddress);
            entry.setValue(entryNeighbours);
        }
    }

    private synchronized void sendNetworkToAll()
    {
        for(InetSocketAddress peerInetSocketAddress: networkMap.keySet())
        {
            sendNetworkToPeer(peerInetSocketAddress);
        }
    }

    private void sendNetworkToPeer(InetSocketAddress peerInetSocketAddress)
    {
        try
        {
            Socket peerSocket = new Socket(peerInetSocketAddress.getAddress(), peerInetSocketAddress.getPort());
            ObjectOutputStream out = new ObjectOutputStream(peerSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(peerSocket.getInputStream());
            
            HashSet<InetSocketAddress> neighbours = networkMap.get(peerInetSocketAddress);
            
            out.writeObject(neighbours);
            
        }
        catch (IOException ex)
        {
            Logger.getLogger(JSHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
