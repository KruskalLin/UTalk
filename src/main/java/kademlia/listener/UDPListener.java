package kademlia.listener;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;


@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class UDPListener extends Listener {

    private final String host;
    private final int port;

    public UDPListener(String host, int port) {
        super(ListenerType.UDP);
        this.host = host;
        this.port = port;
    }
    public UDPListener(String url) {
        super(ListenerType.UDP);
        // udp://host:port
        this.host = url.substring(6, url.lastIndexOf(":"));
        this.port = Integer.parseInt(url.substring(url.lastIndexOf(":")+1));
    }


    public static UDPListener from(String url) {
        return new UDPListener(url);
    }
}
