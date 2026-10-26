package network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import blockchain.BlockChain;

public class Server {

    private final int port;
    private final BlockChain blockChain;
    private final List<Peer> peersList;

    public Server(int port, BlockChain blockChain, List<Peer> peersList) {
        this.port = port;
        this.blockChain = blockChain;
        this.peersList = peersList;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("Servidor iniciado na porta " + port);

            while (true) {
                // Espera um cliente se conectar.
                Socket clientSocket = serverSocket.accept();

                System.out.println("Novo cliente conectado!");

                // Cria um atendente para essa conexão.
                ClientHandler handler = new ClientHandler(
                    clientSocket,
                    blockChain,
                    peersList
                );

                // Inicia o atendimento em uma thread separada.
                handler.start();
            }

        } catch (IOException e) {
            System.err.println("Erro no servidor: " + e.getMessage());
        }
    }
}