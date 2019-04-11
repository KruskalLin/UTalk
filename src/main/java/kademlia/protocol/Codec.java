package kademlia.protocol;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import kademlia.listener.Listener;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;


public class Codec {
    private final Base64.Decoder decoder = Base64.getDecoder();
    private final Base64.Encoder encoder = Base64.getEncoder();

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Message.class, new MessageTypeAdapter())
            .registerTypeAdapter(Listener.class, new ListenerMessageTypeAdapter())
            .create();

    public Message decode(byte[] buffer) throws UnsupportedEncodingException {
        Message message = gson.fromJson(new String(buffer, StandardCharsets.UTF_8).trim(), Message.class);
        return message;
    }

    public byte[] encode(Message msg) {
        return gson.toJson(msg).getBytes();
    }
}
