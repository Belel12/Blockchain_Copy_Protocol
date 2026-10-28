
package network;

import java.util.ArrayList;
import java.util.List;

public class FlowManager {

    // Id deste nó na rede que será enviado ao outro nó (HELLO).
    private final String nodeId;

    // Lista dos peers conhecidos por este nó.
    private final List<Peer> peersList;

    // Guarda a conexão ativa com o peer remoto para que possamos reutilizar a conexão em outros métodos.
    private Client activeClient;

    // Construtor que recebe o id do nó e inicializa a lista de peers vazia.
    public FlowManager(String nodeId) {
        this.nodeId = nodeId;
        this.peersList = new ArrayList<>();
    }

    // Tenta estabelecer uma conexão com um peer e enviar HELLO.
    // Retorna true se o peer responder com ACK.
    // Retorna false se a conexão falhar.
    public boolean connectToPeer(Peer peer) {
        try {
            // Abre uma conexão TCP com o endereço do peer.
            activeClient = new Client(
                peer.getIp(),
                peer.getPort()
            );

            // Envia HELLO com a versão do protocolo e o ID do nó.
            
            activeClient.send(
                MessageFactory.HELLO(
                    NetworkMessage.currentProtocolVersion
                    + "-" + nodeId
                )
            );

            // Aguarda a resposta do peer.
            NetworkMessage response = activeClient.receive();

            // ACK significa que o peer aceitou.
            if (response.getType() == MessageType.ACK) {
                return true;
            }

            // Se a resposta não for ACK, fecha a conexão.
            activeClient.close();
            activeClient = null;

            return false;

        } catch (Exception e) {
            // Informa o erro ocorrido durante a conexão.
            System.err.println(
                "Erro ao conectar ao peer: " + e.getMessage()
            );

            // Indica que a conexão não foi estabelecida corretamente.
            return false;
        }
    }

    // Solicita ao peer conectado a lista de outros peers conhecidos.
    public void requestPeers() throws Exception {

        // Envia uma mensagem GET_PEERS ao peer remoto.
        activeClient.send(MessageFactory.GET_PEERS());

        // Aguarda a resposta do peer remoto.
        NetworkMessage response = activeClient.receive();

        // Confere se a resposta contém a lista de peers.
        if (response.getType() == MessageType.PEERS_LIST) {

            // O payload é um Object; convertemos para List<String>.
            // Cada string representa um peer serializado, como
            // "192.168.1.10:5000".
            @SuppressWarnings("unchecked")
            List<String> serializedPeers =
                (List<String>) response.getPayload();

            // Converte as strings recebidas em objetos Peer e adiciona esses peers à lista local.
            peersList.addAll(
                Peer.desserializePeerList(serializedPeers)
            );

            // Exibe os peers recebidos.
            System.out.println("Peers recebidos: " + peersList);
        }
    }
}

