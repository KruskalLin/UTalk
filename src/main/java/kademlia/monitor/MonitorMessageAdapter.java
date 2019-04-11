package kademlia.monitor;

import com.google.gson.*;
import kademlia.listener.Listener;
import kademlia.listener.ListenerType;

import java.lang.reflect.Type;

public class MonitorMessageAdapter implements JsonDeserializer<MonitorMessage>, JsonSerializer<MonitorMessage> {

    private static final String CLASSNAME = "type";

    @Override
    public MonitorMessage deserialize(JsonElement json, Type typeOfT,
                               JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject =  json.getAsJsonObject();
        JsonPrimitive prim = (JsonPrimitive) jsonObject.get(CLASSNAME);
        String typeName = prim.getAsString();

        return context.deserialize(jsonObject, ListenerType.valueOf(typeName.toUpperCase()).getListenerConfigClass());
    }

    @Override
    public JsonElement serialize(MonitorMessage listener, Type type, JsonSerializationContext jsonSerializationContext) {
        return jsonSerializationContext.serialize(listener);
    }
}
