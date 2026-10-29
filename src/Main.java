
import apps.GenesisApp;
import apps.NodeApp;


public class Main{

    static final String helpMessage = """
    Avaible argument patterns:
    * Genesis node initialization
        - GENESIS [SERVER_PORT]
    * Regular node initialization
        - NODE [NODE_NAME] [SERVER_PORT] [GENESIS_SERVER_IP:PORT ex.: 127.0.0.1:0202]
    * Show this message
        - HELP

    obs: arguments are case transformed to uppercase when processed
    """;
    public static void main(String[] args){

        try{
            String option = args[0].toUpperCase();
            switch (option) {
                case "HELP" -> {
                    System.out.println(helpMessage);
                }
                case "GENESIS" -> {
                    Integer serverPort = Integer.parseInt(args[1]);
                    startGenesisApp(serverPort);
                }
                case "NODE" -> {
                    String nodeId = args[1].toUpperCase();
                    Integer serverPort = Integer.parseInt(args[2]);
                    String[] values = args[3].split(":");
                    String genesisServerIp = values[0];
                    int genesisServerPort = Integer.parseInt(values[1]);
                    startNodeApp(nodeId, serverPort, genesisServerIp, genesisServerPort);
                }
            }
        } catch (ArrayIndexOutOfBoundsException e){
            System.out.println("Invalid number of arguments");
        } catch (NumberFormatException e){
            System.out.println("Invalid port number provided.");
        }

    }

    // auxiliary methods
    static void startGenesisApp(int port){
        try{
            GenesisApp app = new GenesisApp(port);
            app.startApp();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    static void startNodeApp(String nodeName, int serverPort, String genesisServerIp, int genesisServerPort){
        try {
            NodeApp app = new NodeApp(nodeName,serverPort, genesisServerIp, genesisServerPort);
            app.startApp();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
