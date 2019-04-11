import kademlia.Kademlia;
import kademlia.listener.UDPListener;
import kademlia.node.Key;
import kademlia.node.Node;

import java.util.concurrent.TimeoutException;

/**
 * All rights Reserved, Designed by Popping Lim
 *
 * @Author: Popping Lim
 * @Date: 2019/3/21
 * @Todo:
 */
public class Main {
    private static final String[] KEYS = new String[] {
            "738a4793791b8a672050cf495ac15fdae8c5e171",
            "1e8f1fb41a86a828dc14f0f72a97388ecf22d0b0",
            "4e876501a5aa9bc0890aa7b2066a51f011a05bee",
            "6901145bb2f1b655f106b72b1f5351e34d71c96c",
            "6c7950726634ef8b9f0708879067aa935313cebe",
            "2e706bd3d73524e58229ab489ce106834627a6ae"
    };

    public static void main(String args[]) {
        Kademlia kad1 = new Kademlia(
                Key.build(KEYS[0]),
                "udp://localhost:9001"
        );

        try {
            kad1.bootstrap(Node.builder().id(Key.build(KEYS[1])).advertisedListener(
                    new UDPListener("udp://127.0.0.1:9000")
            ).build());
            Key key1 = Key.build(KEYS[1]);
            kad1.send(key1, "发信息");
        }catch (TimeoutException e) {
            e.printStackTrace();
        }
    }
}
