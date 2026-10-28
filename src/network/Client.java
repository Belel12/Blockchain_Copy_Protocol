package network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {

    private Socket socket;
    private ObjectOutputStream objWriter;
    private ObjectInputStream objReader;

    public Client(String ip, int port) throws IOException {
        socket = new Socket(ip, port);

        // Prepara o envio de objetos.
        objWriter = new ObjectOutputStream(
            socket.getOutputStream()
        );
        objWriter.flush();

        // Prepara o recebimento de objetos.
        objReader = new ObjectInputStream(
            socket.getInputStream()
        );
    }

    // Envia uma mensagem para o outro nó.
    public void send(NetworkMessage message) throws IOException {
        objWriter.writeObject(message);
        objWriter.flush();
    }

    // Recebe uma mensagem enviada pelo outro nó.
    public NetworkMessage receive()
            throws IOException, ClassNotFoundException {
        return (NetworkMessage) objReader.readObject();
    }

    // Fecha a conexão e os recursos associados.
    public void close() throws IOException {
        socket.close();
    }
}
