package network;

import java.util.List;
import java.util.ArrayList;
public class Peer{
    private String ip;
    private Integer port;

    public Peer(String ip, Integer port){
        setIp(ip);
        setPort(port);
    }

    public Peer(String serializedPeer){
        String[] values = serializedPeer.split(":");
        String ip = values[0];
        Integer port = Integer.parseInt(values[1]);
        setIp(ip);
        setPort(port);
    }

	public String getIp() {
		return ip;
	}

	private void setIp(String ip) {
		this.ip = ip;
	}

	public Integer getPort() {
		return port;
	}

	private void setPort(Integer port) {
		this.port = port;
	}

    public String serialize(){
        return this.getIp() + ":" + this.getPort().toString();
    }

    public static List<String> serializePeerList(List<Peer> peers){
        List<String> serializedList = new ArrayList<String>();
        for(Peer peer : peers){
            serializedList.add(peer.serialize());
        }
        return serializedList;
    }

    public static List<Peer> desserializePeerList(List<String> serializedList){
        List<Peer> peersList = new ArrayList<Peer>();
        for(String str : serializedList){
            peersList.add(new Peer(str));
        }
        return peersList;
    }
}
