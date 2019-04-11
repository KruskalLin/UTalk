package kademlia.storage;

import lombok.Builder;
import lombok.Data;


@Builder
@Data
public class Value {
    private long lastPublished;
    private String content;
}
