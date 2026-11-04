package apps;

import java.io.IOException;

import utils.Logger;

public class GenesisApp extends BaseApp {

    public GenesisApp(int serverPort) throws IOException {
        super("GENESIS", serverPort);
    }

    @Override
    public void startApp() {

        Logger.info("Starting GenesisApp -> " + getNodeId());

        if (!startServer()) {
            Logger.error("Error while starting the server");
            return;
        }

        // O Genesis também pode minerar e transmitir blocos.
        startUserInputLoop();
    }
}
