package apps;

import java.io.IOException;

import utils.Logger;

public class GenesisApp extends BaseApp {

    public GenesisApp(int serverPort) throws IOException{
        super("GENESIS",serverPort);
    }

    public void startApp(){
        if(!startServer()){
            Logger.error("Error while starting the server");
            return;
        }
        //falta colocar a inicializacao dos fluxos
        startUserInputLoop();
    }
}
