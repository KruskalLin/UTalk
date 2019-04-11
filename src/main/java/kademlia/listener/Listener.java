package kademlia.listener;

import kademlia.exception.UnknownListenerType;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class Listener {
    private ListenerType type;

    public static Listener fromUrl(String url) {
        if(url.startsWith(ListenerType.UDP.prefix())) {
            return UDPListener.from(url);
        }
        else {
            throw new UnknownListenerType(url);
        }
    }
}
