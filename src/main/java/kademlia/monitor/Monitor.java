package kademlia.monitor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

/**
 * All rights Reserved, Designed by Popping Lim
 *
 * @Author: Popping Lim
 * @Date: 2019/3/24
 * @Todo:
 */
public class Monitor {
    private DatagramSocket socket;
    private static int port = 8888;
    private final Gson gson = new GsonBuilder()
            .create();

    public Monitor() throws SocketException {
        this.socket = new DatagramSocket();
    }
    ;

    public void message(String source, String target, String type) throws IOException {
        MonitorMessage monitorMessage = MonitorMessage.builder().type(MonitorType.valueOf(type)).source(source).target(target).build();
        byte[] payload = encode(monitorMessage);
        socket.send(new DatagramPacket(
                payload,
                payload.length,
                new InetSocketAddress("127.0.0.1", port)));
    }


    private MonitorMessage decode(byte[] buffer) throws UnsupportedEncodingException {
        MonitorMessage message = gson.fromJson(new String(buffer, StandardCharsets.UTF_8).trim(), MonitorMessage.class);
        return message;
    }

    private byte[] encode(MonitorMessage msg) {
        return gson.toJson(msg).getBytes();
    }
}
