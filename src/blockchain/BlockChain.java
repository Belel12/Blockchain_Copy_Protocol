package blockchain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BlockChain {

    private List<Block> blockchain;
    private boolean isValid;

    public BlockChain() {
        this.blockchain = new ArrayList<>();
        this.isValid = true;
    }

    public BlockChain(List<Block> chain) {

        if (chain == null) {
            this.blockchain = new ArrayList<>();
            this.isValid = false;
            return;
        }

        this.blockchain = new ArrayList<>(chain);
        validate();
    }

    private List<Block> getBlockchain() {
        return blockchain;
    }

    public synchronized boolean isValid() {
        return isValid;
    }

    public synchronized int size() {
        return blockchain.size();
    }

    /*
     * Valida todos os blocos, inclusive o primeiro.
     * O primeiro bloco utiliza "0" como previousHash.
     */
    private synchronized void validate() {

        if (blockchain == null) {
            isValid = false;
            return;
        }

        for (int i = 0; i < blockchain.size(); i++) {

            Block currentBlock = blockchain.get(i);

            if (currentBlock == null
                    || currentBlock.getHash() == null
                    || currentBlock.getPreviousHash() == null
                    || currentBlock.getData() == null) {

                isValid = false;
                return;
            }

            // Verifica se o hash registrado corresponde ao conteúdo.
            if (!currentBlock.getHash().equals(
                    currentBlock.calculateHash())) {

                isValid = false;
                return;
            }

            // O primeiro bloco deve apontar para o hash inicial.
            if (i == 0) {

                if (!"0".equals(currentBlock.getPreviousHash())) {
                    isValid = false;
                    return;
                }

            } else {

                Block previousBlock = blockchain.get(i - 1);

                // Verifica a ligação entre os blocos.
                if (!previousBlock.getHash().equals(
                        currentBlock.getPreviousHash())) {

                    isValid = false;
                    return;
                }
            }
        }

        isValid = true;
    }

    /*
     * Retorna uma cópia da lista para evitar alterações externas
     * diretamente na blockchain.
     */
    public synchronized List<Block> serialize() {
        return new ArrayList<>(blockchain);
    }

    /*
     * Atualiza a cadeia somente se a nova cadeia for válida
     * e possuir mais blocos.
     */
    public synchronized void update(BlockChain newBlockchain) {

        if (newBlockchain == null || !newBlockchain.isValid()) {
            throw new IllegalArgumentException(
                "Cannot update with an invalid blockchain."
            );
        }

        List<Block> newBlocks = newBlockchain.serialize();

        if (newBlocks.size() <= blockchain.size()) {
            throw new IllegalArgumentException(
                "The received blockchain is not longer."
            );
        }

        blockchain = new ArrayList<>(newBlocks);
        isValid = true;
    }

    /*
     * Cria e adiciona um novo bloco.
     * Se a cadeia estiver vazia, cria o primeiro bloco.
     */
    public synchronized void addBlock(String data, int difficulty) {

        if (data == null || data.isBlank()) {
            throw new IllegalArgumentException(
                "Block data cannot be empty."
            );
        }

        String previousHash = blockchain.isEmpty()
            ? "0"
            : blockchain.get(blockchain.size() - 1).getHash();

        Block newBlock = new Block(
            data,
            previousHash,
            difficulty
        );

        blockchain.add(newBlock);

        validate();

        if (!isValid) {
            blockchain.remove(blockchain.size() - 1);

            throw new IllegalStateException(
                "The newly mined block is invalid."
            );
        }
    }

    public synchronized boolean hasSameChain(BlockChain other) {

        if (other == null || size() != other.size()) {
            return false;
        }

        List<Block> otherBlocks = other.serialize();

        for (int i = 0; i < blockchain.size(); i++) {

            if (!Objects.equals(
                    blockchain.get(i).getHash(),
                    otherBlocks.get(i).getHash())) {

                return false;
            }
        }

        return true;
    }

    @Override
    public synchronized String toString() {

        StringBuilder str = new StringBuilder();

        for (Block block : blockchain) {
            str.append(block.toString()).append("\n");
        }

        return str.toString();
    }
}
