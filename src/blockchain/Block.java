package blockchain;
import java.util.Date;
import java.io.Serializable;
import utils.StringUtil;

public class Block implements Serializable{
    public static final long serialVersionUID = 1L;

    private String hash;
    private String previousHash;
    private String data;
    private long timestamp;
    private int nonce;

    public Block(String data, String previousHash, int difficulty){
        setData(data);
        setPreviousHash(previousHash);
        setTimestamp(new Date().getTime());
        setHash( calculateHash() );
        mineBlock(difficulty);
    }

    protected String calculateHash(){
        return StringUtil.applySHA256(
            this.previousHash
            + Long.toString(this.timestamp)
            + Integer.toString(this.nonce)
            + this.data
        );
    }

    public void mineBlock(int difficulty) {
        String target = new String(new char[difficulty]).replace('\0', '0');
        while(!hash.substring( 0, difficulty).equals(target)) {
            setNonce(nonce++) ;
            setHash( calculateHash() );
        }
        System.out.println("Block Mined!!! : " + hash);
    }

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getHash() {
		return hash;
	}

	public String getPreviousHash() {
		return previousHash;
	}

	public String getData() {
		return data;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public int getNonce() {
		return nonce;
	}

	private void setHash(String hash) {
		this.hash = hash;
	}

	private void setPreviousHash(String previousHash) {
		this.previousHash = previousHash;
	}

	private void setData(String data) {
		this.data = data;
	}

	private void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	private void setNonce(int nonce) {
		this.nonce = nonce;
	}

	public String toString(){
	    return "{\n" +
					"\tHASH: " + getHash() + "\n" +
					"\tPREVIOUS HASH:" + getPreviousHash() + "\n" +
					"\tTIMESTAMP:" + getTimestamp() + "\n" +
					"\tNONCE:" + getNonce() + "\n" +
					"\tDATA:" + getData() + "\n" +
				"}";
	}
}
