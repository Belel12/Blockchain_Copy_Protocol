package network;

import blockchain.Block;
import blockchain.BlockChain;
import config.Config;
import utils.Logger;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler extends Thread {

    private final Socket client;
    private final BlockChain blockChain;
    private final List<Peer> peersList;

    private Peer clientPeer;

    public ClientHandler(
            Socket client,
            BlockChain nodeBlockChain,
            List<Peer> peersList
    ) {

        this.client = client;
        this.blockChain = nodeBlockChain;
        this.peersList = peersList;
    }

    private void handleClient() {

        try (
            this.client;
            ObjectOutputStream objWriter =
                ObjOutStreamWithFlush(this.client.getOutputStream());
            ObjectInputStream objReader =
                new ObjectInputStream(this.client.getInputStream())
        ) {

            while (true) {

                NetworkMessage message =
                    (NetworkMessage) objReader.readObject();

                switch (message.getType()) {

                    case HELLO -> {

                        Logger.info("Received HELLO");

                        Peer identifiedPeer =
                            validateClientNode(
                                (String) message.getPayload()
                            );

                        if (identifiedPeer == null) {

                            objWriter.writeObject(
                                MessageFactory.REJECT(
                                    "Invalid protocol, node ID or port."
                                )
                            );

                            objWriter.flush();

                            return;
                        }

                        clientPeer = identifiedPeer;

                        synchronized (peersList) {

                            if (!peersList.contains(clientPeer)) {
                                peersList.add(clientPeer);
                            }
                        }

                        objWriter.writeObject(
                            MessageFactory.ACK("Node approved")
                        );

                        objWriter.flush();

                        Logger.info(
                            "Approved peer: " + clientPeer
                        );
                    }

                    case GET_PEERS -> {

                        if (clientPeer == null) {
                            sendReject(
                                objWriter,
                                "HELLO must be sent first."
                            );
                            return;
                        }

                        Logger.info(
                            "Received GET_PEERS from " + clientPeer
                        );

                        List<Peer> availablePeers =
                            new ArrayList<>();

                        synchronized (peersList) {

                            for (Peer peer : peersList) {

                                // Não devolve o próprio solicitante.
                                if (!peer.equals(clientPeer)) {
                                    availablePeers.add(peer);
                                }
                            }
                        }

                        objWriter.writeObject(
                            MessageFactory.PEERS_LIST(
                                Peer.serializePeerList(availablePeers)
                            )
                        );

                        objWriter.flush();

                        Logger.info(
                            "Sent PEERS_LIST to " + clientPeer
                        );
                    }

                    case GET_BLOCKCHAIN -> {

                        if (clientPeer == null) {
                            sendReject(
                                objWriter,
                                "HELLO must be sent first."
                            );
                            return;
                        }

                        Logger.info(
                            "Received GET_BLOCKCHAIN from " + clientPeer
                        );

                        objWriter.writeObject(
                            MessageFactory.BLOCKCHAIN_COPY(
                                blockChain.serialize()
                            )
                        );

                        objWriter.flush();

                        Logger.info(
                            "Sent BLOCKCHAIN_COPY to " + clientPeer
                        );
                    }

                    case BLOCKCHAIN_COPY -> {

                        if (clientPeer == null) {
                            sendReject(
                                objWriter,
                                "HELLO must be sent first."
                            );
                            return;
                        }

                        Logger.info(
                            "Received BLOCKCHAIN_COPY from " + clientPeer
                        );

                        try {

                            @SuppressWarnings("unchecked")
                            List<Block> receivedBlocks =
                                (List<Block>) message.getPayload();

                            BlockChain receivedBlockchain =
                                new BlockChain(receivedBlocks);

                            if (!receivedBlockchain.isValid()) {

                                sendReject(
                                    objWriter,
                                    "Blockchain is invalid."
                                );

                            } else if (
                                receivedBlockchain.size()
                                    > blockChain.size()
                            ) {

                                blockChain.update(receivedBlockchain);

                                objWriter.writeObject(
                                    MessageFactory.ACK(
                                        "Blockchain approved and updated."
                                    )
                                );

                                objWriter.flush();

                                Logger.info(
                                    "Blockchain updated from " + clientPeer
                                );

                            } else if (
                                blockChain.hasSameChain(receivedBlockchain)
                            ) {

                                objWriter.writeObject(
                                    MessageFactory.ACK(
                                        "Blockchain already up to date."
                                    )
                                );

                                objWriter.flush();

                            } else {

                                sendReject(
                                    objWriter,
                                    "Blockchain is not longer than local chain."
                                );
                            }

                        } catch (ClassCastException | NullPointerException e) {

                            sendReject(
                                objWriter,
                                "Invalid blockchain payload."
                            );
                        }
                    }

                    case REQUEST_CONNECTION_END -> {

                        Logger.info(
                            "Received REQUEST_CONNECTION_END from "
                            + clientPeer
                        );

                        objWriter.writeObject(
                            MessageFactory.END_CONNECTION()
                        );

                        objWriter.flush();

                        Logger.info(
                            "Connection ended with " + clientPeer
                        );

                        return;
                    }

                    default -> {

                        sendReject(
                            objWriter,
                            "Invalid message type."
                        );
                    }
                }
            }

        } catch (EOFException e) {

            Logger.info(
                "Connection closed by peer: " + clientPeer
            );

        } catch (Exception e) {

            Logger.error(
                "Error handling client " + clientPeer
                + ": " + e.getMessage()
            );
        }
    }

    /*
     * Interpreta:
     * versão|nodeId|porta
     *
     * Retorna null se os dados forem inválidos.
     */
    private Peer validateClientNode(String payload) {

        if (payload == null || payload.isBlank()) {
            return null;
        }

        String[] values = payload.split("\\|", -1);

        if (values.length != 3) {
            return null;
        }

        String protocolVersion = values[0];
        String nodeId = values[1];
        String portString = values[2];

        if (!Config.currentProtocolVersion.equals(protocolVersion)
                || nodeId.isBlank()) {
            return null;
        }

        try {

            int peerPort = Integer.parseInt(portString);

            if (peerPort < 1 || peerPort > 65535) {
                return null;
            }

            String peerIp =
                client.getInetAddress().getHostAddress();

            return new Peer(peerIp, peerPort);

        } catch (NumberFormatException e) {

            return null;
        }
    }

    private void sendReject(
            ObjectOutputStream objWriter,
            String reason
    ) throws IOException {

        objWriter.writeObject(
            MessageFactory.REJECT(reason)
        );

        objWriter.flush();

        Logger.info("Sent REJECT: " + reason);
    }

    @Override
    public void run() {
        handleClient();
    }

    private ObjectOutputStream ObjOutStreamWithFlush(
            OutputStream outStream
    ) throws IOException {

        ObjectOutputStream objout =
            new ObjectOutputStream(outStream);

        objout.flush();

        return objout;
    }
}
