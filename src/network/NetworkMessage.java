package network;

import java.io.Serializable;

public class NetworkMessage implements Serializable{
    public static final long serialVersionUID = 1L;

    public MessageType type;
    public Object payload;

    public NetworkMessage(MessageType type, Object payload){
        this.type = type;
        this.payload = payload;
    }
}
