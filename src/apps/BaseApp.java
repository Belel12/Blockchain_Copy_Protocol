package apps;

import java.util.List;
import java.util.Scanner;
import java.io.IOException;
import java.lang.Thread;

import blockchain.BlockChain;
import config.Config;
import network.Peer;
import network.Server;

import utils.Logger;

public abstract class BaseApp {

    private  String     nodeId;
    private  Server     serverInstance;

    private static String userInputRequest = "Execute command (type h to see options) : ";
    private static String options = "1 - Inspect this node's BlockChain\n2 - Inspect this node's known Peers\n 3 - Abort this node's execution";

    public BaseApp (
        String nodeId,
        int serverPort
    ) throws IOException {
        this.nodeId = nodeId;
        this.serverInstance = new Server(serverPort);
    }

    //must be single print statemant since other threads can print in between
    public void printBlockChain(){
        System.out.println("This node's blockchain: \n" + serverInstance.getBlockChain().toString());
    }

    public void printKnownPeers(){
        System.out.println("This node's known peers: \n" + serverInstance.getPeersList().toString());
    }

    protected void abortNodeExecution(){
        try{
            Logger.userInputLoopActive = false;
            //interrupts flow manager
            serverInstance.interrupt();
        } catch(IOException e) {
            Logger.error("Error while aborting node execution");
            e.printStackTrace();
        }
    }

    public void startUserInputLoop(){
        try(Scanner scanner = new Scanner(System.in)){
            Logger.userInputLoopActive = true;
            System.out.print(getUserInputRequest());
            while(scanner.hasNextLine()){
                String opt = scanner.nextLine().trim();
                switch (opt){
                    case "h" -> {
                        System.out.println(options);
                    }
                    case "1" -> {
                        printBlockChain();
                    }
                    case "2" -> {
                        printKnownPeers();
                    }
                    case "3" -> {
                        abortNodeExecution();
                        return;
                    }
                    default -> {
                        System.out.println("Unknow option: " + opt);
                    }
                }
                System.out.print(getUserInputRequest());
            }
        }
    }


    protected boolean startServer(){
        Logger.info("Starting server...");
        new Thread(serverInstance).start();
        try{
            Thread.sleep(2 * 1000); // time to let the server start in the other thread
        } catch(InterruptedException e){e.printStackTrace();} //unlikely to happen

        return serverInstance.isRunning();
    }

    // must call startServer and validate before
    public abstract void startApp();

    public String getVersionIdString(){
        return Config.currentProtocolVersion + "-" + getNodeId();
    }
	public BlockChain getBlockChain() {
		return serverInstance.getBlockChain();
	}

	public List<Peer> getKnownPeers() {
		return serverInstance.getPeersList();
	}

	public String getNodeId() {
		return nodeId;
	}

	public int getServerPort() {
		return serverInstance.getPort();
	}

	public static String getUserInputRequest() {
		return userInputRequest;
	}

}
