package misc;

public class Messages {
    public static final String peerStarted = "Peer Started.";
    public static final String commandPrompt = "\nEnter a command:";
    public static final String connect = "connect";
    public static final String get = "get";
    public static final String leave = "leave";
    public static final String help = "help";
    public static final String exit = "exit";
    public static final String commandNotFound = "\nERROR: Command not understood.";
    public static final String options =
            "\nOptions: \n"
                    + "  - Connect <Peer IP Addr> <Peer Port #>\n"
                    + "  - Get <File Name>\n"
                    + "  - Leave\n"
                    + "  - Help\n"
                    + "  - Exit";
    public static final String connectionAccepted = "\nConnection accepted from address: %s, port: %d.";
    public static final String queryReceived = "\nReceived Query from address: %s, port: %d.";
    public static final String query = "Q:%d;%s\n";
    public static final String queryResponsePositive = "\nFile \"%s\" found";
    public static final String queryResponseNegative = "\nFile \"%s\" not found";
    public static final String response = "R:%d;(%s:%d);%s\n";
    public static final String connectionAttempted = "\nAttempting to connect to peer at %s:%d.";
    public static final String connectionSuccessful = "\nConnection to peer at %s:%d successful!";
    public static final String connectionFailed = "\nConnection to peer at %s:%d failed!";
    public static final String heartbeat = "H:\n";
    public static final String sendingHeartbeat = "\nSending heartbeat to peer %s:%d.";
    public static final String heartbeatReceived = "\nHeartbeat received from peer %s:%d.";
    public static final String replyingHeartbeat = "\nReplying to Heartbeat.";
    public static final String heartbeatReplyReceived = "\nHeartbeat reply received";
    public static final String heartbeatNoReturn = "\nHeartbeat did not return within timeout value. Closing connection to %s:%d.";
    public static final String getUsage = "\nUsage: Get <file name>";
    public static final String querySend = "\nSending query to %s:%d...";
    public static final String fileTransferRequestSend = "\nSending file transfer request to peer %s:%d for file %s.";
    public static final String fileTransferRequestReceived = "\nReceived file transfer request from peer %s:%d for file %s.";
    public static final String fileTransmissionSuccessful = "\nFile transmission completed. File %s sent to peer %s:%d";
    public static final String fileTransferRequest = "T:%s\n";
    public static final String peerDiscoveryRequest = "PI:%s:%d";
    public static final String peerDiscoveryResponse = "PO:%s:%d";
    public static final String connectUsage = "\nUsage: Connect <peer IP addr> <peer Port#>";
}
