package com.nmj.ws;

import static spark.Spark.init;
import static spark.Spark.webSocket;

public class Application {

    public static void main(String[] args) {
        webSocket("/app", WsHandler.class);
        init();
    }

}
