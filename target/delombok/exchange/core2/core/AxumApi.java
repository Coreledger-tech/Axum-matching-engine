package exchange.core2.core;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import exchange.core2.core.common.OrderAction;
import exchange.core2.core.common.OrderType;
import exchange.core2.core.common.api.ApiPlaceOrder;
import exchange.core2.core.common.cmd.CommandResultCode;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;

public class AxumApi {

    private static ExchangeApi api;

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // Initialize the Axum matching engine
        Axum.main(args); // Start the Axum matching engine
        api = Axum.getApi();

        try {
            // Start WebSocket server
            WebSocketServer server = new AxumWebSocketServer(new InetSocketAddress(9091), api);
            server.start();
            System.out.println("WebSocket server started on port 9091");
        } catch (Exception e) {
            System.err.println("Failed to start WebSocket server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

class AxumWebSocketServer extends WebSocketServer {

    private final ExchangeApi api;

    public AxumWebSocketServer(InetSocketAddress address, ExchangeApi api) {
        super(address);
        this.api = api;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("WebSocket connection opened: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("WebSocket connection closed: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Received WebSocket message: " + message);

        try {
            // Parse JSON message
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            String type = json.get("type").getAsString();

            if ("place-order".equals(type)) {
                String symbol = json.get("symbol").getAsString();
                String orderTypeStr = json.get("orderType").getAsString();
                String actionStr = json.get("action").getAsString();
                long price = json.get("price").getAsLong();
                long quantity = json.get("quantity").getAsLong();
                long userId = json.get("userId").getAsLong();

                OrderType orderType = OrderType.valueOf(orderTypeStr);
                OrderAction action = OrderAction.valueOf(actionStr);

                api.submitCommandAsync(ApiPlaceOrder.builder()
                        .uid(userId)
                        .orderId(System.nanoTime())
                        .symbol(Integer.parseInt(symbol))
                        .price(price)
                        .size(quantity)
                        .action(action)
                        .orderType(orderType)
                        .build()).thenAccept(result -> {
                    JsonObject response = new JsonObject();
                    response.addProperty("type", "place-order-response");
                    response.addProperty("status", result == CommandResultCode.SUCCESS ? "success" : "failure");
                    response.addProperty("message", result.name());

                    conn.send(response.toString());
                });
            } else {
                conn.send("{\"type\": \"error\", \"message\": \"Unknown message type\"}");
            }
        } catch (Exception e) {
            conn.send("{\"type\": \"error\", \"message\": \"" + e.getMessage() + "\"}");
            System.err.println("Error processing WebSocket message: " + e.getMessage());
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("WebSocket error: " + ex.getMessage());
    }

    @Override
    public void onStart() {
        System.out.println("WebSocket server started");
    }
}
