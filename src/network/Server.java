package network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

import blockchain.BlockChain;

import utils.Logger;

public class Server {

    private final int port;
    private final BlockChain blockChain;
    private final List<Peer> peersList;
    private boolean running;
    private final ServerSocket serverSocket;
    public Server(int port, BlockChain blockChain, List<Peer> peersList) throws IOException{
        this.port = port;
        this.blockChain = blockChain;
        this.peersList = peersList;
        this.running = true;
        this.serverSocket = new ServerSocket(this.port);
    }

    public void start() {
        try (serverSocket) {

            Logger.info("Servidor iniciado na porta " + port);

            while (running) {
                // Espera um cliente se conectar.
                Socket clientSocket = serverSocket.accept();

                Logger.info("Novo cliente conectado!");

                // Cria um atendente para essa conexão.
                ClientHandler handler = new ClientHandler(
                    clientSocket,
                    blockChain,
                    peersList
                );

                // Inicia o atendimento em uma thread separada.
                handler.start();
            }

        } catch (SocketException e){
            if (!running){
                Logger.info("Server interrupted");
                return;
            }
            e.printStackTrace();
        } catch (IOException e) {
            Logger.error("Erro no servidor: " + e.getMessage());
        }
    }

    public void interrupt(){
        try {
            if(!serverSocket.isClosed()){
                serverSocket.close();
            }
        } catch (IOException e){
            Logger.error("Error while closing server");
            e.printStackTrace();
        }
    }
}
