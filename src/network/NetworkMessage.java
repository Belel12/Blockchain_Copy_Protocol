package network;

import java.io.Serializable;

public class NetworkMessage implements Serializable{
    public static final long serialVersionUID = 1L;
    public static final String currentProtocolVersion = "V1.0";

    private MessageType type;
    private Object payload;

    protected NetworkMessage(MessageType type, Object payload){
        setType(type);
        setPayload(payload);
    }

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public MessageType getType() {
		return type;
	}

	private void setType(MessageType type) {
		this.type = type;
	}

	public Object getPayload() {
		return payload;
	}

	private void setPayload(Object payload) {
		this.payload = payload;
	}
}
