package network;

import java.util.List;
import blockchain.Block;

public class MessageFactory{
    public static NetworkMessage HELLO(String payload){
        return new NetworkMessage(MessageType.HELLO, payload);
    }

    public static NetworkMessage ACK(String payload){
        return new NetworkMessage(MessageType.ACK, payload);
    }

    public static NetworkMessage GET_PEERS(){
        return new NetworkMessage(MessageType.GET_PEERS, null);
    }

    public static NetworkMessage PEERS_LIST(List<String> payload){
        return new NetworkMessage(MessageType.PEERS_LIST, payload);
    }

    public static NetworkMessage GET_BLOCKCHAIN(){
        return new NetworkMessage(MessageType.GET_BLOCKCHAIN, null);
    }

    public static NetworkMessage BLOCKCHAIN_COPY(List<Block> payload){
        return new NetworkMessage(MessageType.BLOCKCHAIN_COPY, payload);
    }

    public static NetworkMessage REJECT(String payload){
        return new NetworkMessage(MessageType.REJECT, payload);
    }

    public static NetworkMessage REQUEST_CONNECTION_END(){
        return new NetworkMessage(MessageType.REQUEST_CONNECTION_END, null);
    }

    public static NetworkMessage END_CONNECTION(){
        return new NetworkMessage(MessageType.END_CONNECTION, null);
    }
}
