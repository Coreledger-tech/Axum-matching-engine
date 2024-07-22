package exchange.core2.core;

import exchange.core2.core.common.OrderAction;
import exchange.core2.core.common.OrderType;
import exchange.core2.core.common.api.ApiCancelOrder;
import exchange.core2.core.common.api.ApiPlaceOrder;
import exchange.core2.core.common.cmd.CommandResultCode;
import quickfix.*;
import quickfix.Message;
import quickfix.field.Side;
import quickfix.fix44.*;


public class FixApp implements Application {
    private final ExchangeApi exchangeApi;

    public FixApp(ExchangeApi exchangeApi) {
        this.exchangeApi = exchangeApi;
    }

    @Override
    public void onCreate(SessionID sessionId) {
        System.out.println("Session created: " + sessionId);
    }

    @Override
    public void onLogon(SessionID sessionId) {
        System.out.println("Logon: " + sessionId);
    }

    @Override
    public void onLogout(SessionID sessionId) {
        System.out.println("Logout: " + sessionId);
    }

    @Override
    public void toAdmin(Message message, SessionID sessionId) {
        System.out.println("Admin message sent: " + message);
    }

    @Override
    public void fromAdmin(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
        System.out.println("Admin message received: " + message);
    }

    @Override
    public void toApp(Message message, SessionID sessionId) throws DoNotSend {
        System.out.println("App message sent: " + message);
    }

    @Override
    public void fromApp(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        System.out.println("App message received: " + message);

        if (message instanceof NewOrderSingle) {
            NewOrderSingle order = (NewOrderSingle) message;
            handleNewOrder(order);
        } else if (message instanceof OrderCancelRequest) {
            handleOrderCancel((OrderCancelRequest) message);
        } else if (message instanceof OrderCancelReplaceRequest) {
            handleOrderReplace((OrderCancelReplaceRequest) message);
        } else if (message instanceof OrderStatusRequest) {
            handleOrderStatusRequest((OrderStatusRequest) message);
        } else if (message instanceof MarketDataRequest) {
            handleMarketDataRequest((MarketDataRequest) message);
        }
    }

    private void handleNewOrder(NewOrderSingle order) throws FieldNotFound {
        String symbol = order.getSymbol().getValue();
        char side = order.getSide().getValue();
        double price = order.getPrice().getValue();
        double quantity = order.getOrderQty().getValue();

        // Translate FIX message to Axum command
        // Example:
        // Assume symbol, side, price, and quantity mapping is done here
        long userId = 301L; // Example user ID
        OrderAction orderAction = (side == Side.BUY) ? OrderAction.BID : OrderAction.ASK;
        OrderType orderType = OrderType.GTC; // Example order type

        // Place order using exchange core API
        exchangeApi.submitCommandAsync(ApiPlaceOrder.builder()
                .uid(userId)
                .orderId(System.nanoTime()) // Example order ID
                .symbol(1001) // Example symbol ID
                .price((long) (price * 100)) // Price in cents
                .size((long) quantity)
                .action(orderAction)
                .orderType(orderType)
                .build());
    }

    private void handleOrderCancel(OrderCancelRequest cancel) throws FieldNotFound {
        String origClOrdID = cancel.getOrigClOrdID().getValue();
        long orderId = Long.parseLong(origClOrdID);

        // Cancel order using Axum API
        exchangeApi.submitCommandAsync(ApiCancelOrder.builder()
                .orderId(orderId)
                .build());
    }

    private void handleOrderReplace(OrderCancelReplaceRequest replace) throws FieldNotFound {
        String origClOrdID = replace.getOrigClOrdID().getValue();
        long orderId = Long.parseLong(origClOrdID);
        String symbol = replace.getSymbol().getValue();
        char side = replace.getSide().getValue();
        double price = replace.getPrice().getValue();
        double quantity = replace.getOrderQty().getValue();

        OrderAction orderAction = (side == Side.BUY) ? OrderAction.BID : OrderAction.ASK;
        OrderType orderType = OrderType.GTC; // Example order type

        // Replace order using Axum API
        exchangeApi.submitCommandAsync(ApiCancelOrder.builder()
                .orderId(orderId)
                .build()).thenAccept(cancelResult -> {
            if (cancelResult == CommandResultCode.SUCCESS) {
                exchangeApi.submitCommandAsync(ApiPlaceOrder.builder()
                        .uid(301L) // Example user ID
                        .orderId(System.nanoTime()) // New order ID
                        .symbol(1001) // Example symbol ID, map from actual symbol if needed
                        .price((long) (price * 100)) // Price in cents
                        .size((long) quantity)
                        .action(orderAction)
                        .orderType(orderType)
                        .build());
            }
        });
    }

    private void handleOrderStatusRequest(OrderStatusRequest request) throws FieldNotFound {
        String clOrdID = request.getClOrdID().getValue();
        long orderId = Long.parseLong(clOrdID);

        // Example: Fetch and respond with order status using Axum API
//        exchangeApi.getOrderStatus(orderId).thenAccept(status -> {
            // Construct and send ExecutionReport message based on status
       // });
    }

    private void handleMarketDataRequest(MarketDataRequest request) throws FieldNotFound {
        // Example: Process market data request using Axum API
        // Construct and send MarketDataSnapshotFullRefresh message
    }
}

