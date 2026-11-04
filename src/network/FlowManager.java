package network;

import java.util.ArrayList;
import java.util.List;

import blockchain.Block;
import blockchain.BlockChain;
import config.Config;

public class FlowManager {

    private final String nodeId;
    private final int serverPort;
    private final List<Peer> peersList;
    private final BlockChain blockChain;

    private Client activeClient;

    public FlowManager(
            String nodeId,
            int serverPort,
            List<Peer> peersList,
            BlockChain blockChain
    ) {

        this.nodeId = nodeId;
        this.serverPort = serverPort;
        this.peersList = peersList;
        this.blockChain = blockChain;
    }

    /*
     * Formato do HELLO:
     * versão|identificação do nó|porta do servidor
     */
    private String createHelloPayload() {

        return Config.currentProtocolVersion
            + "|" + nodeId
            + "|" + serverPort;
    }

    /*
     * Estabelece conexão com um peer e realiza o handshake.
     */
    public boolean connectToPeer(Peer peer) {

        closeActiveConnection();

        try {

            activeClient = new Client(
                peer.getIp(),
                peer.getPort()
            );

            activeClient.send(
                MessageFactory.HELLO(createHelloPayload())
            );

            NetworkMessage response = activeClient.receive();

            if (response.getType() == MessageType.ACK) {
                System.out.println(
                    "Conexão estabelecida com " + peer
                );
                return true;
            }

            System.err.println(
                "Peer rejeitou conexão: " + peer
            );

            closeActiveConnection();
            return false;

        } catch (Exception e) {

            System.err.println(
                "Erro ao conectar ao peer " + peer
                + ": " + e.getMessage()
            );

            closeActiveConnection();
            return false;
        }
    }

    /*
     * Fluxo A: solicita a lista de peers conhecidos.
     */
    public void requestPeers() throws Exception {

        if (activeClient == null) {
            throw new IllegalStateException(
                "No active peer connection."
            );
        }

        activeClient.send(MessageFactory.GET_PEERS());

        NetworkMessage response = activeClient.receive();

        if (response.getType() != MessageType.PEERS_LIST) {
            throw new IllegalStateException(
                "Unexpected response to GET_PEERS: "
                + response.getType()
            );
        }

        @SuppressWarnings("unchecked")
        List<String> serializedPeers =
            (List<String>) response.getPayload();

        List<Peer> receivedPeers =
            Peer.desserializePeerList(serializedPeers);

        synchronized (peersList) {

            for (Peer peer : receivedPeers) {

                if (!peersList.contains(peer)) {
                    peersList.add(peer);
                }
            }
        }

        System.out.println(
            "Peers conhecidos: " + getPeersSnapshot()
        );
    }

    /*
     * Fluxo B: solicita a blockchain de um peer.
     */
    public void requestBlockchain() throws Exception {

        if (activeClient == null) {
            throw new IllegalStateException(
                "No active peer connection."
            );
        }

        activeClient.send(MessageFactory.GET_BLOCKCHAIN());

        NetworkMessage response = activeClient.receive();

        if (response.getType() != MessageType.BLOCKCHAIN_COPY) {
            throw new IllegalStateException(
                "Unexpected response to GET_BLOCKCHAIN: "
                + response.getType()
            );
        }

        @SuppressWarnings("unchecked")
        List<Block> receivedBlocks =
            (List<Block>) response.getPayload();

        BlockChain receivedBlockchain =
            new BlockChain(receivedBlocks);

        if (!receivedBlockchain.isValid()) {

            System.out.println(
                "Blockchain recebida é inválida."
            );

        } else if (
                receivedBlockchain.size() > blockChain.size()) {

            blockChain.update(receivedBlockchain);

            System.out.println(
                "Blockchain atualizada com sucesso."
            );

        } else if (blockChain.hasSameChain(receivedBlockchain)) {

            System.out.println(
                "Blockchain local já está atualizada."
            );

        } else {

            System.out.println(
                "Blockchain local possui tamanho igual ou superior."
            );
        }
    }

    /*
     * Fluxo C: transmite a blockchain local a todos os peers.
     */
    public void broadcastBlockchain() {

        List<Peer> peers = getPeersSnapshot();

        if (peers.isEmpty()) {
            System.out.println(
                "Nenhum peer disponível para broadcast."
            );
            return;
        }

        // Cria uma cópia consistente da cadeia antes de transmiti-la.
        List<Block> blockchainCopy = blockChain.serialize();

        System.out.println(
            "Iniciando broadcast para " + peers.size() + " peer(s)."
        );

        for (Peer peer : peers) {

            Client peerClient = null;

            try {

                System.out.println(
                    "Enviando blockchain para " + peer
                );

                peerClient = new Client(
                    peer.getIp(),
                    peer.getPort()
                );

                // Primeiro realiza o handshake.
                peerClient.send(
                    MessageFactory.HELLO(createHelloPayload())
                );

                NetworkMessage helloResponse =
                    peerClient.receive();

                if (helloResponse.getType() != MessageType.ACK) {

                    System.err.println(
                        "Peer rejeitou HELLO: " + peer
                    );

                    continue;
                }

                // Envia a cópia da blockchain.
                peerClient.send(
                    MessageFactory.BLOCKCHAIN_COPY(blockchainCopy)
                );

                NetworkMessage response =
                    peerClient.receive();

                if (response.getType() == MessageType.ACK) {

                    System.out.println(
                        "Blockchain aceita por " + peer
                    );

                } else if (response.getType() == MessageType.REJECT) {

                    System.out.println(
                        "Blockchain rejeitada por " + peer
                        + ": " + response.getPayload()
                    );

                } else {

                    System.err.println(
                        "Resposta inesperada de " + peer
                        + ": " + response.getType()
                    );
                }

            } catch (Exception e) {

                System.err.println(
                    "Erro no broadcast para " + peer
                    + ": " + e.getMessage()
                );

            } finally {

                if (peerClient != null) {
                    closeConnection(peerClient);
                }
            }
        }

        System.out.println("Broadcast finalizado.");
    }

    /*
     * Retorna uma cópia da lista de peers.
     */
    private List<Peer> getPeersSnapshot() {

        synchronized (peersList) {
            return new ArrayList<>(peersList);
        }
    }

    /*
     * Encerra uma conexão específica.
     */
    private void closeConnection(Client client) {

        try {

            client.send(
                MessageFactory.REQUEST_CONNECTION_END()
            );

            NetworkMessage response = client.receive();

            if (response.getType() != MessageType.END_CONNECTION) {
                System.err.println(
                    "Resposta inesperada ao encerrar conexão."
                );
            }

        } catch (Exception e) {

            System.err.println(
                "Erro durante encerramento da conexão: "
                + e.getMessage()
            );

        } finally {

            try {
                client.close();
            } catch (Exception e) {
                System.err.println(
                    "Erro ao fechar socket: " + e.getMessage()
                );
            }
        }
    }

    /*
     * Encerra a conexão ativa usada pelos Fluxos A e B.
     */
    public void closeActiveConnection() {

        if (activeClient != null) {

            closeConnection(activeClient);
            activeClient = null;
        }
    }
}
