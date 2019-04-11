package kademlia.listener;


public enum ListenerType {
    UDP("udp", UDPListener.class);

    private final String prefix;
    private final Class listenerConfigClass;

    ListenerType(String prefix, Class<UDPListener> listenerConfigClass) {
        this.prefix = prefix;
        this.listenerConfigClass = listenerConfigClass;
    }

    public String prefix() {
        return prefix;
    }

    public Class getListenerConfigClass() {
        return listenerConfigClass;
    }
}
