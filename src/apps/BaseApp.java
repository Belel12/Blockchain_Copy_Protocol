package apps;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import blockchain.BlockChain;
import config.Config;
import network.FlowManager;
import network.Peer;
import network.Server;
import utils.Logger;

public abstract class BaseApp {

    private final String nodeId;
    private final Server serverInstance;
    private final FlowManager flowManager;

    private static final String userInputRequest =
        "Execute command (type h to see options) : ";

    private static final String options =
        "1 - Inspect this node's BlockChain\n"
        + "2 - Inspect this node's known Peers\n"
        + "3 - Abort this node's execution\n"
        + "4 - Mine a new block";

    public BaseApp(String nodeId, int serverPort) throws IOException {

        this.nodeId = nodeId;
        this.serverInstance = new Server(serverPort);

        // O gerenciador é criado uma vez para cada aplicação.
        this.flowManager = new FlowManager(
            nodeId,
            serverPort,
            serverInstance.getPeersList(),
            serverInstance.getBlockChain()
        );
    }

    public void printBlockChain() {

        BlockChain bc = getBlockChain();

        if (bc.size() < 1) {

            System.out.println("No blocks added yet");

        } else {

            System.out.println(
                "This node's blockchain:\n" + bc
            );
        }
    }

    public void printKnownPeers() {

        System.out.println(
            "This node's known peers:\n" + getKnownPeers()
        );
    }

    /*
     * Minera um bloco e inicia o Fluxo C.
     */
    protected void mineNewBlock(Scanner scanner) {

        System.out.print("Enter block data: ");

        String data = scanner.nextLine().trim();

        if (data.isEmpty()) {
            System.out.println("Block data cannot be empty.");
            return;
        }

        try {

            System.out.println("Starting mining...");

            getBlockChain().addBlock(
                data,
                Config.miningDifficulty
            );

            System.out.println(
                "Block added successfully. Blockchain size: "
                + getBlockChain().size()
            );

            // A propagação acontece somente após a mineração.
            getFlowManager().broadcastBlockchain();

        } catch (Exception e) {

            Logger.error(
                "Error mining or broadcasting block: "
                + e.getMessage()
            );
        }
    }

    protected void abortNodeExecution() {

        try {

            Logger.userInputLoopActive = false;

            // Fecha a conexão de saída, caso exista.
            flowManager.closeActiveConnection();

            serverInstance.interrupt();

        } catch (IOException e) {

            Logger.error("Error while aborting node execution");
            e.printStackTrace();
        }
    }

    public void startUserInputLoop() {

        try (Scanner scanner = new Scanner(System.in)) {

            Logger.userInputLoopActive = true;

            System.out.print(getUserInputRequest());

            while (scanner.hasNextLine()) {

                String opt = scanner.nextLine().trim();

                switch (opt) {

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

                    case "4" -> {
                        mineNewBlock(scanner);
                    }

                    default -> {
                        System.out.println("Unknown option: " + opt);
                    }
                }

                System.out.print(getUserInputRequest());
            }
        }
    }

    protected boolean startServer() {

        Logger.info("Starting server...");

        new Thread(serverInstance).start();

        try {

            Thread.sleep(1000);

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
            Logger.error("Interrupted while starting server.");
            return false;
        }

        return serverInstance.isRunning();
    }

    public abstract void startApp();

    public String getVersionIdString() {
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

    public Server getServerInstance() {
        return serverInstance;
    }

    public FlowManager getFlowManager() {
        return flowManager;
    }

    public static String getUserInputRequest() {
        return userInputRequest;
    }
}
