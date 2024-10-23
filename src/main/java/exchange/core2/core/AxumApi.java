package exchange.core2.core;
import static spark.Spark.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import exchange.core2.core.ExchangeApi;
import exchange.core2.core.common.OrderAction;
import exchange.core2.core.common.OrderType;
import exchange.core2.core.common.api.ApiCancelOrder;
import exchange.core2.core.common.api.ApiOrderBookRequest;
import exchange.core2.core.common.api.ApiPlaceOrder;
import exchange.core2.core.common.cmd.CommandResultCode;

import java.util.concurrent.ExecutionException;

public class AxumApi {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // Initialize the Axum matching engine
        Axum.main(args);  // Start the Axum matching engine
        port(9090);
        // Get the API instance
        ExchangeApi api = Axum.getApi();

        // Endpoint for placing orders
//        post("/api/place-order", (req, res) -> {
//            try {
//                String symbol = req.queryParams("symbol");
//                String orderTypeStr = req.queryParams("orderType");
//                String actionStr = req.queryParams("action");
//                long price = Long.parseLong(req.queryParams("price"));
//                long quantity = Long.parseLong(req.queryParams("quantity"));
//                long userId = Long.parseLong(req.queryParams("userId"));
//
//                OrderType orderType = OrderType.valueOf(orderTypeStr);
//                OrderAction action = OrderAction.valueOf(actionStr);
//
//                api.submitCommandAsync(ApiPlaceOrder.builder()
//                        .uid(userId)
//                        .orderId(System.nanoTime())
//                        .symbol(Integer.parseInt(symbol))
//                        .price(price)
//                        .size(quantity)
//                        .action(action)
//                        .orderType(orderType)
//                        .build()).thenAccept(result -> {
//                    if (result == CommandResultCode.SUCCESS) {
//                        res.status(200);
//                        res.body("Trade successfully placed");
//                    } else {
//                        res.status(400);
//                        res.body("Failed to place trade: " + result.name());
//                    }
//                });
//            } catch (Exception e) {
//                res.status(500);
//                res.body("Internal Server Error: " + e.getMessage());
//            }
//            return res.body();
//        });

        post("/api/place-order", (req, res) -> {
            System.out.println("Received place-order request");
            try {
                // Parse JSON body
                JsonObject jsonBody = JsonParser.parseString(req.body()).getAsJsonObject();
                String symbol = jsonBody.get("symbol").getAsString();
                String orderTypeStr = jsonBody.get("orderType").getAsString();
                String actionStr = jsonBody.get("action").getAsString();
                long price = jsonBody.get("price").getAsLong();
                long quantity = jsonBody.get("quantity").getAsLong();
                long userId = jsonBody.get("userId").getAsLong();

                OrderType orderType = OrderType.valueOf(orderTypeStr);
                OrderAction action = OrderAction.valueOf(actionStr);

                // Call the matching engine's API to submit the order
                api.submitCommandAsync(ApiPlaceOrder.builder()
                        .uid(userId)
                        .orderId(System.nanoTime())
                        .symbol(Integer.parseInt(symbol))
                        .price(price)
                        .size(quantity)
                        .action(action)
                        .orderType(orderType)
                        .build()).thenAccept(result -> {
                    if (result == CommandResultCode.SUCCESS) {
                        res.status(200);
                        res.body("Trade successfully placed");
                    } else {
                        res.status(400);
                        res.body("Failed to place trade: " + result.name());
                    }
                });
            } catch (Exception e) {
                res.status(500);
                res.body("Internal Server Error: " + e.getMessage());
                System.err.println("Error processing place-order: " + e.getMessage());
            }
            return res.body();
        });


        // Endpoint for canceling orders
        post("/api/cancel-order", (req, res) -> {
            try {
                long orderId = Long.parseLong(req.queryParams("orderId"));
                long symbol = Long.parseLong(req.queryParams("symbol"));
                long userId = Long.parseLong(req.queryParams("userId"));

                api.submitCommandAsync(ApiCancelOrder.builder()
                        .orderId(orderId)
                        .uid(userId)
                        .symbol((int) symbol)
                        .build()).thenAccept(result -> {
                    if (result == CommandResultCode.SUCCESS) {
                        res.status(200);
                        res.body("Order successfully canceled");
                    } else {
                        res.status(400);
                        res.body("Failed to cancel order: " + result.name());
                    }
                });
            } catch (Exception e) {
                res.status(500);
                res.body("Internal Server Error: " + e.getMessage());
            }
            return res.body();
        });

        // Endpoint for retrieving the order book
        get("/api/order-book", (req, res) -> {
            try {
                int symbol = Integer.parseInt(req.queryParams("symbol"));
                int depth = Integer.parseInt(req.queryParams("depth"));

                api.requestOrderBookAsync(symbol, depth).thenAccept(orderBook -> {
                    if (orderBook != null) {
                        res.status(200);
                        res.body("Order book data: " + orderBook.toString());
                    } else {
                        res.status(400);
                        res.body("Failed to retrieve order book");
                    }
                });
            } catch (Exception e) {
                res.status(500);
                res.body("Internal Server Error: " + e.getMessage());
            }
            return res.body();
        });
    }
}
