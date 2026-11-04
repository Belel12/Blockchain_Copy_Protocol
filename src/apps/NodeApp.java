package apps;

import java.io.IOException;

import network.Peer;
import utils.Logger;

public class NodeApp extends BaseApp {

    public NodeApp(
            String nodeId,
            int nodeServerPort,
            String genesisPeerString
    ) throws IOException {

        super(nodeId, nodeServerPort);

        Peer genesisPeer = new Peer(genesisPeerString);

        getKnownPeers().add(genesisPeer);
    }

    @Override
    public void startApp() {

        Logger.info("Starting NodeApp -> " + getNodeId());

        if (!startServer()) {
            Logger.error("Error while starting the server");
            return;
        }

        Peer genesis = getKnownPeers().get(0);

        if (getFlowManager().connectToPeer(genesis)) {

            try {

                // Fluxo A.
                getFlowManager().requestPeers();

                // Fluxo B.
                getFlowManager().requestBlockchain();

            } catch (Exception e) {

                Logger.error(
                    "Error while requesting data: "
                    + e.getMessage()
                );

            } finally {

                getFlowManager().closeActiveConnection();
            }

        } else {

            Logger.error("Could not connect to Genesis");
        }

        startUserInputLoop();
    }
}
