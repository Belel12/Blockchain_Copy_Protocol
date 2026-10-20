package blockchain;

import java.util.List;

public class BlockChain {

    private List<Block> blockchain;
    private boolean isValid;

    public BlockChain(){
        setValid(true);
    }

    public BlockChain(List<Block> chain){
        setBlockchain(chain);
        validate();
    }


	private List<Block> getBlockchain() {
		return blockchain;
	}

	private void setBlockchain(List<Block> blockchain) {
		this.blockchain = blockchain;
	}

	public boolean isValid() {
		return isValid;
	}

	private void setValid(boolean isValid) {
		this.isValid = isValid;
	}

	public int size(){
	    return getBlockchain().size();
	}

	private void validate() {
        Block currentBlock;
        Block previousBlock;
        //loop through blockchain to check hashes:
        for(int i=1; i < blockchain.size(); i++) {
            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i-1);
            //compare registered hash and calculated hash:
            if(!currentBlock.getHash().equals(currentBlock.calculateHash()) ){
                System.out.println("Current Hashes not equal");
                setValid(false);
                return;
            }
            //compare previous hash and registered previous hash
            if(!previousBlock.getHash().equals(currentBlock.getPreviousHash())) {
                System.out.println("Previous Hashes not equal");
                setValid(false);
                return;
            }
        }
        setValid(true);
    }

    public List<Block> serialize(){
        return getBlockchain();
    }

    public void update(List<Block> newBlockchain){
        setBlockchain(newBlockchain);
    }

    public void addBlock(String data, int difficulty){
        getBlockchain().add(new Block(
            data,
            getBlockchain().getLast().getHash(),
            difficulty
        ));
        validate();
    }
}
