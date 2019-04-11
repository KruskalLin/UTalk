package kademlia.monitor;

import kademlia.protocol.MessageType;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class MonitorMessage {
    private MonitorType type;
    private String source;
    private String target;
}
