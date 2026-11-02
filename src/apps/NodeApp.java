package apps;

import java.io.IOException;

import network.Peer;
import network.FlowManager;
import utils.Logger;

public class NodeApp extends BaseApp {

    private FlowManager flowManager;

    public NodeApp(
            String nodeId,
            int nodeServerPort,
            String genesisPeerString
    ) throws IOException, ArrayIndexOutOfBoundsException {

        super(nodeId, nodeServerPort);

        // Adiciona o Genesis à lista inicial de peers.
        Peer genesisPeer = new Peer(genesisPeerString);
        getServerInstance().getPeersList().add(genesisPeer);
    }

    public void startApp() {

        Logger.info("Starting NodeApp -> " + getNodeId());

        // Inicia o servidor deste nó.
        if (!startServer()) {
            Logger.error("Error while starting the server");
            return;
        }

        // Inicializa o FlowManager com as estruturas do próprio nó.
        flowManager = new FlowManager(
            getNodeId(),
            getKnownPeers(),
            getBlockChain()
        );

        // O primeiro peer da lista é o Genesis.
        Peer genesis = getKnownPeers().get(0);

        // Tenta conectar ao Genesis.
        if (flowManager.connectToPeer(genesis)) {

            try {

                // Flow A: solicita os peers conhecidos pelo Genesis.
                flowManager.requestPeers();

                // Flow B: solicita a blockchain do Genesis.
                flowManager.requestBlockchain();

            } catch (Exception e) {

                Logger.error(
                    "Error while requesting data: " + e.getMessage()
                );
            }

        } else {

            Logger.error("Could not connect to Genesis");
        }

        // Inicia o menu de interação com o usuário.
        startUserInputLoop();
    }
}
