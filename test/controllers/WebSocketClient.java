package controllers;

import play.shaded.ahc.org.asynchttpclient.AsyncHttpClient;
import play.shaded.ahc.org.asynchttpclient.BoundRequestBuilder;
import play.shaded.ahc.org.asynchttpclient.ListenableFuture;
import play.shaded.ahc.org.asynchttpclient.netty.ws.NettyWebSocket;
import play.shaded.ahc.org.asynchttpclient.ws.WebSocket;
import play.shaded.ahc.org.asynchttpclient.ws.WebSocketListener;
import play.shaded.ahc.org.asynchttpclient.ws.WebSocketUpgradeHandler;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Copied from https://raw.githubusercontent.com/playframework/play-samples/2.8.x/play-java-websocket-example/test/controllers/WebSocketClient.java
 */
public class WebSocketClient {

    private AsyncHttpClient client;

    public WebSocketClient(AsyncHttpClient c) {
        this.client = c;
    }

    public NettyWebSocket call(String url, WebSocketListener listener) throws Exception {
        final BoundRequestBuilder requestBuilder = client.prepareGet(url);
        final WebSocketUpgradeHandler handler = new WebSocketUpgradeHandler.Builder().addWebSocketListener(listener).build();
        final ListenableFuture<NettyWebSocket> future = requestBuilder.execute(handler);
        return future.toCompletableFuture().get(30, TimeUnit.SECONDS);
    }

    static class LoggingListener implements WebSocketListener {
        private final Consumer<String> onMessageCallback;

        public LoggingListener(Consumer<String> onMessageCallback) {
            this.onMessageCallback = onMessageCallback;
        }

        private Throwable throwableFound = null;

        public Throwable getThrowable() {
            return throwableFound;
        }

        public void onOpen(WebSocket websocket) {
            // do nothing
        }

        @Override
        public void onClose(WebSocket webSocket, int i, String s) {
            // do nothing
        }

        public void onError(Throwable t) {
            // do nothing
            throwableFound = t;
        }

        @Override
        public void onTextFrame(String payload, boolean finalFragment, int rsv) {
            //logger.info("onMessage: s = " + s);
            onMessageCallback.accept(payload);
        }
    }
}