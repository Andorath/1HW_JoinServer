package joinserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
    private InetSocketAddress joiningPeerInetSocketAddress;
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
        try
        {
            joiningPeerInetSocketAddress = (InetSocketAddress) in.readObject();
            
            addPeer();
        }
        catch (IOException | ClassNotFoundException ex)
        {
            Logger.getLogger(JSHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally
        {
            if(!clientSocket.isClosed())
            {
                try
                {
                    clientSocket.close();
                }
                catch (IOException ex)
                {
                    Logger.getLogger(JSHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
    }
    
    private synchronized void addPeer()
    {         
         if (!networkMap.containsKey(joiningPeerInetSocketAddress))
         {
             HashSet<InetSocketAddress> neighbours = new HashSet<>();
             
             neighbours.addAll(networkMap.keySet());
             updateOtherNeighboursWith();
             networkMap.put(joiningPeerInetSocketAddress, neighbours);
             
             sendNetworkToAll();
         }
    }

    private synchronized void updateOtherNeighboursWith()
    {
        for (Map.Entry<InetSocketAddress, HashSet<InetSocketAddress>> entry : networkMap.entrySet())
        {
            HashSet<InetSocketAddress> entryNeighbours = entry.getValue();
            entryNeighbours.add(joiningPeerInetSocketAddress);
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

    private synchronized void sendNetworkToPeer(InetSocketAddress peerInetSocketAddress)
    {
        Socket peerSocket = null;
        
        try 
        {
            if(peerInetSocketAddress.equals(joiningPeerInetSocketAddress))
            {
                this.out.writeObject(networkMap.get(joiningPeerInetSocketAddress));
            }
            else
            {
                peerSocket = new Socket(peerInetSocketAddress.getAddress(), peerInetSocketAddress.getPort());
                ObjectOutputStream out = new ObjectOutputStream(peerSocket.getOutputStream());
                HashSet<InetSocketAddress> neighbours = networkMap.get(peerInetSocketAddress);
                out.writeObject(neighbours);
            }
            
        }
        catch (IOException ex)
        {
            Logger.getLogger(JSHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally
        {
            if(!peerSocket.isClosed())
            {
                try
                {
                    peerSocket.close();
                }
                catch (IOException ex)
                {
                    Logger.getLogger(JSHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
    }
    
}
