package apps;

import java.io.IOException;

import network.Peer;
import utils.Logger;

public class NodeApp extends BaseApp{

    public NodeApp(String nodeId, int nodeServerPort, String genesisPeerString) throws IOException, ArrayIndexOutOfBoundsException{
        super(nodeId, nodeServerPort);
        Peer genesisPeer = new Peer(genesisPeerString);
        getServerInstance().getPeersList().add(genesisPeer);
    }

    public void startApp(){
        Logger.info("Starting NodeApp -> " + getNodeId());
        if(!startServer()){
            Logger.error("Error while starting the server");
            return;
        }
        //falta colocar a inicializacao dos fluxos do Nó regular
        startUserInputLoop();
    }
}
