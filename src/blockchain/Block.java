package blockchain;

import java.io.Serializable;
import java.util.Date;

import utils.StringUtil;

public class Block implements Serializable {

    private static final long serialVersionUID = 1L;

    private String hash;
    private String previousHash;
    private String data;
    private long timestamp;
    private int nonce;

    public Block(String data, String previousHash, int difficulty) {

        if (data == null || previousHash == null) {
            throw new IllegalArgumentException(
                "Data and previous hash cannot be null."
            );
        }

        if (difficulty < 0 || difficulty > 64) {
            throw new IllegalArgumentException(
                "Mining difficulty must be between 0 and 64."
            );
        }

        this.data = data;
        this.previousHash = previousHash;
        this.timestamp = new Date().getTime();
        this.nonce = 0;

        this.hash = calculateHash();

        mineBlock(difficulty);
    }

    protected String calculateHash() {

        return StringUtil.applySHA256(
            previousHash
            + Long.toString(timestamp)
            + Integer.toString(nonce)
            + data
        );
    }

    public void mineBlock(int difficulty) {

        if (difficulty < 0 || difficulty > 64) {
            throw new IllegalArgumentException(
                "Invalid mining difficulty."
            );
        }

        String target = "0".repeat(difficulty);

        while (!hash.startsWith(target)) {

            if (nonce == Integer.MAX_VALUE) {
                throw new IllegalStateException(
                    "Nonce limit reached."
                );
            }

            // Incrementa o nonce antes de calcular o novo hash.
            nonce++;

            hash = calculateHash();
        }

        System.out.println("Block Mined!!! : " + hash);
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

    public String toString() {

        return "{\n"
            + "\tHASH: " + getHash() + "\n"
            + "\tPREVIOUS HASH: " + getPreviousHash() + "\n"
            + "\tTIMESTAMP: " + getTimestamp() + "\n"
            + "\tNONCE: " + getNonce() + "\n"
            + "\tDATA: " + getData() + "\n"
            + "}";
    }
}
