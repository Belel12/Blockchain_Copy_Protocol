package network;

import blockchain.*;
import utils.Logger;
import java.net.Socket;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.Thread;
import java.util.List;
import config.Config;

public class ClientHandler extends Thread{

    private final Socket client;
    private final BlockChain blockChain;
    private final List<Peer> peersList;
    public ClientHandler(Socket client, BlockChain nodeBlockChain, List<Peer> peersList){
        this.client = client;
        this.blockChain = nodeBlockChain;
        this.peersList = peersList;
    }

    private void handleClient(){
        Peer clientPeer = new Peer(client.getInetAddress().getHostName(), client.getPort());
        try(
            this.client;
            ObjectOutputStream objWriter = ObjOutStreamWithFlush(this.client.getOutputStream());
            ObjectInputStream objReader = new ObjectInputStream(this.client.getInputStream());
        ){
            NetworkMessage message;
            //main loop
            while(true){
                message = (NetworkMessage) objReader.readObject();
                switch (message.getType()){
                    case HELLO -> {
                        Logger.info("Received HELLO from " + clientPeer);
                        if(validateClientNode((String) message.getPayload())){
                            objWriter.writeObject(MessageFactory.ACK("Node approved"));
                            objWriter.flush();
                            Logger.info("Sent ACK(Node approved) to " + clientPeer);
                        }
                        else{
                            objWriter.writeObject(MessageFactory.REJECT("Your protocol version is outdated or you provided a null Node id"));
                            objWriter.flush();
                            Logger.info("Sent REJECT(Node protocol or id invalid) to " + clientPeer);
                        }
                    }
                    case GET_PEERS -> {
                        Logger.info("Received GET_PEERS from " + clientPeer);
                        objWriter.writeObject(MessageFactory.PEERS_LIST(Peer.serializePeerList(peersList)));
                        objWriter.flush();
                        Logger.info("Sent PEER_LIST to " + clientPeer);
                        if(!peersList.contains(clientPeer)){
                            peersList.add(clientPeer);
                            Logger.info("Added " + clientPeer + " to this node's Peers List");
                        }
                    }
                    case GET_BLOCKCHAIN -> {
                        Logger.info("Received GET_BLOCKCHAIN from " + clientPeer);
                        objWriter.writeObject(MessageFactory.BLOCKCHAIN_COPY(blockChain.serialize()));
                        objWriter.flush();
                        Logger.info("Sent BLOCKCHAIN_COPY to " + clientPeer);
                    }
                    case BLOCKCHAIN_COPY -> {
                        Logger.info("Received BLOCKCHAIN_COPY from " + clientPeer);
                        @SuppressWarnings("unchecked")
                        BlockChain clientBlockChain = new BlockChain((List<Block>) message.getPayload());
                        if(clientBlockChain.isValid() && clientBlockChain.size() > this.blockChain.size()){
                            this.blockChain.update(clientBlockChain);
                            objWriter.writeObject(MessageFactory.ACK("Blockchain approved and updated"));
                            objWriter.flush();
                            Logger.info("Sent ACK(Blockchain approved) to " + clientPeer);
                        }
                        else{
                            String reason = clientBlockChain.isValid() ? "BlockChain size lower than local's" : "BlockChain is invalid";
                            objWriter.writeObject(MessageFactory.REJECT(reason));
                            objWriter.flush();
                            Logger.info("Sent REJECT("+reason+") to " + clientPeer);
                        }
                    }
                    case REQUEST_CONNECTION_END -> {
                        Logger.info("Received REQUEST_CONNECTION_END from " + clientPeer);
                        objWriter.writeObject(MessageFactory.END_CONNECTION());
                        objWriter.flush();
                        Logger.info("Sent END_CONNECTION to " + clientPeer);
                        Logger.info("Ended connection with " + clientPeer);
                        break;
                    }
                    default -> {
                        Logger.info("Received invalid message type from client: " +  message.getType());
                        objWriter.writeObject(MessageFactory.REJECT("Invalid message type"));
                        objWriter.flush();
                        Logger.info("Sent REJECT(Invalid message type) to " + clientPeer);
                    }
                }
            }

        } catch(EOFException eof) {
            Logger.error(clientPeer + " ended connection abruptly while client handler tried to read object. Handling was cancelled normally.");
        } catch(Exception e) {
            Logger.error("An unexpected error happenned during client handle in " + clientPeer + " \n" + e);
        }
    }

    //validates both protocol of client and his ID (just checks if its not null or empty)
    private boolean validateClientNode(String clientNodeId){
        if(clientNodeId == null)
            return false;
        String[] values = clientNodeId.split("-");
        String clientProtocolVersion = values[0];
        String clientID = values[1];
        if(!clientProtocolVersion.equals(Config.currentProtocolVersion) || clientID == null || clientID.isEmpty())
            return false;
        return true;
    }

    @Override
    public void run(){
        handleClient();
    }

    private ObjectOutputStream ObjOutStreamWithFlush(OutputStream outStream) throws IOException{
        ObjectOutputStream objout = new ObjectOutputStream(outStream);
        objout.flush();
        return objout;
    }
}
