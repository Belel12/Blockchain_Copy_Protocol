import java.util.LinkedList;
//import com.google.gson.GsonBuilder;

import blockchain.Block;

public class App {

    public static final int difficulty = 20;
    public static LinkedList<Block> blockchain = new LinkedList<Block>();

    public static void main(String[] args) throws Exception {
       //add our blocks to the blockchain ArrayList:
        blockchain.add(new Block("Hi im the first block", "0"));
        blockchain.add(new Block("Second Block", blockchain.getLast().hash));
        blockchain.add(new Block("Third Block", blockchain.getLast().hash));
        blockchain.add(new Block("Fourth Block", blockchain.getLast().hash));

        String blockchainjson = new GsonBuilder().setPrettyPrinting().create().toJson(blockchain);
        System.out.println(blockchainjson);

        blockchain.getFirst().mineBlock(difficulty);

    }

    public static Boolean isChainValid() {
        Block currentBlock;
        Block previousBlock;
        //loop through blockchain to check hashes:
        for(int i=1; i < blockchain.size(); i++) {
            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i-1);
            //compare registered hash and calculated hash:
            if(!currentBlock.hash.equals(currentBlock.calculateHash()) ){
                System.out.println("Current Hashes not equal");
                return false;
            }
            //compare previous hash and registered previous hash
            if(!previousBlock.hash.equals(currentBlock.previousHash) ) {
                System.out.println("Previous Hashes not equal");
                return false;
            }
        }
        return true;
    }
}
