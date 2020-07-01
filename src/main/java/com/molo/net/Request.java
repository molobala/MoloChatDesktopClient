package com.molo.net;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;


/**
 * Created by Molobala on 29/03/2017.
 */

public class Request {
    public  static enum RequestType{
        GET_ALL_CONNECTED_MEMBERS,
        GET_LOGIN,
        GET_ALL_MESSAGE, SEND_MESSAGE, SET_LOGIN, GET_MESSAGE,
        GET_ALL_THREAD,
        GET_THREAD, REGISTER,
        NONE, LOG_WITH_KEY, AUTHENTIFICATION
    }
    public static ObjectMapper mapper=new ObjectMapper();
    public ObjectNode object;
    public RequestType type;
    public String command;
    public long hash;
    public Request(long hash,String command,RequestType type){
        this.hash=hash;
        this.object=mapper.createObjectNode();
        this.type=type;
        this.command=command;
        object.put("hash",hash);
        object.put("command",command);
    }
    @Override
    public String toString() {
        return object.toString();
    }
}