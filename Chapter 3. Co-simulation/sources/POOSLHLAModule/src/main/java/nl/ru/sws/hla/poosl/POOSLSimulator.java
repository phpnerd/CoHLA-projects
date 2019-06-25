package nl.ru.sws.hla.poosl;

import com.google.gson.Gson;
import hla.HLASimulator;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.function.Consumer;

/**
 * Created by thomas on 4-4-16.
 */
public class POOSLSimulator implements HLASimulator {

    private Thread responseThread;
    private List<Consumer<HLAMessage>> responseListeners;
    protected final List<HLAAttribute> attributes;

    private String hostname;
    private int port;

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    private Gson gson;

    public POOSLSimulator(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
        this.gson = new Gson();
        responseListeners = new ArrayList<>();
        attributes = new ArrayList<>();
    }

    public void init() {
        connect();
        responseThread = new Thread(() -> {
            while(!responseThread.isInterrupted())
                try {
                    HLAMessage message = gson.fromJson(reader.readLine(), HLAMessage.class);
                    responseListeners.forEach(l -> l.accept(message));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
        });
    }

    public void start() {
        responseThread.start();
    }

    @Override
    public void stop() {
        try {
            if (responseThread != null)
                responseThread.interrupt();
            if (writer != null)
                writer.close();
            if (reader != null)
                reader.close();
            if (socket != null)
                socket.close();
            System.out.println("Connection closed.");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void advanceTime(double time) {
        HLAMessage message = new HLAMessage(HLAMessageType.TIME, HLAMessage.STR_TIMEADVANCEGRANT);
        message.addParameter(time);
        sendMessage(message);
    }

    public void addResponseListener(Consumer<HLAMessage> listener) {
        responseListeners.add(listener);
    }

    public void removeResponseListener(Consumer<HLAMessage> listener) {
        responseListeners.remove(listener);
    }

    public void sendMessage(HLAMessage message) {
        String json = gson.toJson(message);
        try {
            writer.write(json + "\n");
            writer.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void sendInteraction(String name, Object... parameters) {
        HLAMessage message = new HLAMessage(HLAMessageType.INTERACTION, name, Arrays.asList(parameters));
        sendMessage(message);
    }

    public void syncAttribute(HLAAttribute attribute) {
        if (attribute.doWrite()) {
            HLAMessage message = new HLAMessage(HLAMessageType.ATTRIBUTE, attribute.getName(), Collections.singletonList(attribute.getGetter().get()));
            sendMessage(message);
        } else {
            attribute.setSynced(false);
            HLAMessage message = new HLAMessage(HLAMessageType.ATTRIBUTE, attribute.getName());
            sendMessage(message);
        }
    }

    public void syncAttributes() {
        attributes.forEach(this::syncAttribute);
        while(attributes.stream().filter(a -> !a.isSynced()).count() > 0)
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
    }

    private void connect() {
        if (socket != null)
            return;
        System.out.println("Connecting to " + hostname + ":" + port + "...");
        try {
            socket = new Socket(hostname, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            System.out.println("Connected.");
        } catch (IOException ex) {
            System.err.println("Failed to connect. Retrying...");
            try {
                Thread.sleep(1000);
                connect();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
