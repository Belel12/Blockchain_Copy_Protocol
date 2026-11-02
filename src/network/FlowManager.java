
package network;

import java.util.List;

import blockchain.Block;
import blockchain.BlockChain;
import config.Config;

public class FlowManager {

    // ID deste nó na rede, enviado durante o HELLO.
    private final String nodeId;

    // Lista compartilhada dos peers conhecidos pelo nó.
    private final List<Peer> peersList;

    // Blockchain compartilhada com o Server.
    private final BlockChain blockChain;

    // Conexão ativa com o peer remoto.
    private Client activeClient;


    // Construtor: recebe os dados e as estruturas do próprio nó.
    public FlowManager(
            String nodeId,
            List<Peer> peersList,
            BlockChain blockChain
    ) {
        this.nodeId = nodeId;
        this.peersList = peersList;
        this.blockChain = blockChain;
    }


    // Estabelece uma conexão com um peer e envia HELLO.
    // Retorna true se receber ACK.
    public boolean connectToPeer(Peer peer) {

        try {

            // Abre uma conexão TCP com o peer.
            activeClient = new Client(
                peer.getIp(),
                peer.getPort()
            );

            // Envia a versão do protocolo e o ID deste nó.
            activeClient.send(
                MessageFactory.HELLO(
                    Config.currentProtocolVersion + "-" + nodeId
                )
            );

            // Aguarda a resposta do peer.
            NetworkMessage response = activeClient.receive();

            // Verifica se o peer aceitou a conexão.
            if (response.getType() == MessageType.ACK) {
                return true;
            }

            // Fecha a conexão caso não receba ACK.
            activeClient.close();
            activeClient = null;

            return false;

        } catch (Exception e) {

            System.err.println(
                "Erro ao conectar ao peer: " + e.getMessage()
            );

            // Tenta fechar a conexão caso tenha sido aberta.
            try {
                if (activeClient != null) {
                    activeClient.close();
                }
            } catch (Exception closeError) {
                System.err.println("Erro ao fechar conexão.");
            }

            // Limpa a referência do cliente.
            activeClient = null;

            return false;
        }
    }


    // Solicita ao peer conectado a lista de peers conhecidos.
    public void requestPeers() throws Exception {

        // Envia a solicitação da lista de peers.
        activeClient.send(MessageFactory.GET_PEERS());

        // Aguarda a resposta.
        NetworkMessage response = activeClient.receive();

        // Verifica se recebeu uma lista de peers.
        if (response.getType() == MessageType.PEERS_LIST) {

            // Converte o payload para uma lista de Strings.
            @SuppressWarnings("unchecked")
            List<String> serializedPeers =
                (List<String>) response.getPayload();

            // Converte as Strings recebidas em objetos Peer.
            List<Peer> receivedPeers =
                Peer.desserializePeerList(serializedPeers);

            // Adiciona somente peers ainda não conhecidos.
            for (Peer peer : receivedPeers) {
                if (!peersList.contains(peer)) {
                    peersList.add(peer);
                }
            }

            System.out.println("Peers recebidos: " + peersList);

        } else {

            System.err.println(
                "Resposta inesperada ao solicitar peers: "
                + response.getType()
            );
        }
    }


    // Solicita a blockchain do peer conectado.
    public void requestBlockchain() throws Exception {

        // Envia a solicitação da blockchain.
        activeClient.send(MessageFactory.GET_BLOCKCHAIN());

        // Aguarda a resposta do peer.
        NetworkMessage response = activeClient.receive();

        // Verifica se recebeu uma cópia da blockchain.
        if (response.getType() == MessageType.BLOCKCHAIN_COPY) {

            // Converte o payload recebido para uma lista de blocos.
            @SuppressWarnings("unchecked")
            List<Block> receivedBlocks =
                (List<Block>) response.getPayload();

            // Cria uma blockchain com os blocos recebidos.
            // O construtor executa a validação da cadeia.
            BlockChain receivedBlockchain =
                new BlockChain(receivedBlocks);

            // Verifica se a cadeia recebida é válida
            // e possui mais blocos que a cadeia local.
            if (receivedBlockchain.isValid()
                    && receivedBlockchain.size() > blockChain.size()) {

                // Atualiza a blockchain compartilhada com o Server.
                blockChain.update(receivedBlockchain);

                System.out.println(
                    "Blockchain atualizada com sucesso."
                );

            } else if (!receivedBlockchain.isValid()) {

                System.out.println(
                    "Blockchain recebida é inválida."
                );

            } else {

                System.out.println(
                    "Blockchain local já está atualizada ou possui mais blocos."
                );
            }

        } else {

            // Informa se o peer enviou uma resposta inesperada.
            System.err.println(
                "Resposta inesperada ao solicitar blockchain: "
                + response.getType()
            );
        }
    }
}
