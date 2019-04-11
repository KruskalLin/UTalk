package kademlia.exception;

/**
 * All rights Reserved, Designed by Popping Lim
 *
 * @Author: Popping Lim
 * @Date: 2019/3/22
 * @Todo:
 */
public class UnknownMessageType extends RuntimeException{
    public UnknownMessageType() {
        super("Unknown Message Type.");
    }
}
