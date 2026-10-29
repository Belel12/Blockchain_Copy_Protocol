package network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.ArrayList;
import java.lang.Runnable;

import blockchain.BlockChain;

import utils.Logger;

public class Server implements Runnable {

    private final int port;
    private final BlockChain blockChain;
    private final List<Peer> peersList;

    private boolean running;
    private final ServerSocket serverSocket;
    public Server(int port) throws IOException{
        this.port = port;
        this.blockChain = new BlockChain();
        this.peersList = new ArrayList<Peer>();
        this.running = true;
        this.serverSocket = new ServerSocket(this.port);
    }

    public void run(){
        try (serverSocket) {

            Logger.info("Servidor iniciado na porta " + port);

            while (running) {
                // Espera um cliente se conectar.
                Socket clientSocket = serverSocket.accept();

                Logger.info("Novo cliente conectado!");

                // Cria um atendente para essa conexão.
                ClientHandler handler = new ClientHandler(
                    clientSocket,
                    this.blockChain,
                    this.peersList
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
        } catch(IOException e){
            Logger.error("Erro no servidor: " + e.getMessage());
        } finally {
            running = false;
            Logger.info("Server stopped");
        }
    }

    public void interrupt() throws IOException{
        running = false;
        serverSocket.close();
    }

    public BlockChain getBlockChain(){
        return this.blockChain;
    }

    public List<Peer> getPeersList(){
        return this.peersList;
    }

    public int getPort(){
        return this.port;
    }

    public boolean isRunning(){
        return this.running;
    }
}
