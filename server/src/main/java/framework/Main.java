package framework;

import io.undertow.Undertow;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

import java.util.Deque;

public class Main {
    public static void main(String[] args) {
        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler((HttpServerExchange exchange) -> {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html");

                    String response = "";

                    Deque<String> aParams = exchange.getQueryParameters().get("a");
                    Deque<String> bParams = exchange.getQueryParameters().get("b");

                    if (aParams != null && !aParams.isEmpty()
                            && bParams != null && !bParams.isEmpty()) {
                        response = "a = " + aParams.getFirst() + ", b = " + bParams.getFirst();
                    }

                    exchange.getResponseSender().send(response);

                }).build();
        server.start();
    }
}
