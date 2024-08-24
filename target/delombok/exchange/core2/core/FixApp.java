package exchange.core2.core;

import exchange.core2.core.common.OrderAction;
import exchange.core2.core.common.OrderType;
import exchange.core2.core.common.api.ApiCancelOrder;
import exchange.core2.core.common.api.ApiPlaceOrder;
import exchange.core2.core.common.cmd.CommandResultCode;
import quickfix.*;
import quickfix.Message;
import quickfix.field.*;
import quickfix.fix44.*;


public class FixApp implements Application {
    private final ExchangeApi exchangeApi;
    private SessionID currentSessionId;

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

       try {
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
        } catch (Exception e) {
            System.err.println("Error processing FIX message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleNewOrder(NewOrderSingle order) throws FieldNotFound {
        String symbol = order.getSymbol().getValue();
        char side = order.getSide().getValue();
        double price = order.getPrice().getValue();
        double quantity = order.getOrderQty().getValue();

        System.out.println("Processing NewOrderSingle: symbol=" + symbol + ", side=" + side + ", price=" + price + ", quantity=" + quantity);

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
                // .build());
                .build()).thenAccept(result -> {
                    if (result != CommandResultCode.SUCCESS) {
                        System.err.println("Failed to place order: " + result);
                    }
                });                       
    }

    private void handleOrderCancel(OrderCancelRequest cancel) throws FieldNotFound {
        String origClOrdID = cancel.getOrigClOrdID().getValue();
        long orderId = Long.parseLong(origClOrdID);

        System.out.println("Processing OrderCancelRequest: origClOrdID=" + origClOrdID);

        // Cancel order using Axum API
        exchangeApi.submitCommandAsync(ApiCancelOrder.builder()
                .orderId(orderId)
                // .build());
                .build()).thenAccept(result -> {
                    if (result != CommandResultCode.SUCCESS) {
                        System.err.println("Failed to cancel order: " + result);
                    }
                });                       
    }

    private void handleOrderReplace(OrderCancelReplaceRequest replace) throws FieldNotFound {
        String origClOrdID = replace.getOrigClOrdID().getValue();
        long orderId = Long.parseLong(origClOrdID);
        String symbol = replace.getSymbol().getValue();
        char side = replace.getSide().getValue();
        double price = replace.getPrice().getValue();
        double quantity = replace.getOrderQty().getValue();

        System.out.println("Processing OrderCancelReplaceRequest: origClOrdID=" + origClOrdID + ", symbol=" + symbol + ", side=" + side + ", price=" + price + ", quantity=" + quantity);

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
                        // .build());
                        .build()).thenAccept(result -> {
                            if (result != CommandResultCode.SUCCESS) {
                                System.err.println("Failed to replace order: " + result);
                            }
                        });
            } else {
                System.err.println("Failed to cancel order for replacement: " + cancelResult);
            }                       
        });
    }

    private void handleBatchOrder(Message message, SessionID sessionId) throws FieldNotFound {
        // Extract the repeating group of orders (NoOrders)
        int noOrders = message.getInt(NoOrders.FIELD);

        for (int i = 1; i <= noOrders; i++) {
            Group orderGroup = message.getGroup(i, NoOrders.FIELD);

            // Extract individual order details from the group
            String clOrdID = orderGroup.getString(ClOrdID.FIELD);
            String symbol = orderGroup.getString(Symbol.FIELD);
            char side = orderGroup.getChar(Side.FIELD);
            double orderQty = orderGroup.getDouble(OrderQty.FIELD);
            double price = orderGroup.getDouble(Price.FIELD);

            System.out.println("Processing batch order: clOrdID=" + clOrdID + ", symbol=" + symbol + ", side=" + side + ", price=" + price + ", quantity=" + orderQty);

            // Translate FIX message to ExchangeCore order
            long userId = 301L; // Example user ID, you may want to map this based on session or other data
            OrderAction orderAction = (side == Side.BUY) ? OrderAction.BID : OrderAction.ASK;
            OrderType orderType = OrderType.GTC; // Example order type, modify as needed

            // Convert the symbol string to your internal symbol ID (this mapping needs to be done based on your setup)
            long symbolId = getSymbolIdForBond(symbol); // Example mapping function

            // Place each order using the exchange API
            exchangeApi.submitCommandAsync(ApiPlaceOrder.builder()
                    .uid(userId)
                    .orderId(System.nanoTime()) // Unique order ID for each order
                    .symbol((int) symbolId)
                    .price((long) (price * 100)) // Adjust price based on precision
                    .size((long) orderQty)
                    .action(orderAction)
                    .orderType(orderType)
                    .build()).thenAccept(result -> {
                if (result != CommandResultCode.SUCCESS) {
                    System.err.println("Failed to place order: " + result);
                } else {
                    System.out.println("Batch order placed successfully: clOrdID=" + clOrdID);
                }
            });
        }
    }

    private long getSymbolIdForBond(String symbol) {
        // Example mapping for bond symbols
        switch (symbol) {
            case "US9128285M80": // Example ISIN for a US government bond
                return 1003; // Return corresponding internal symbol ID
            case "CORP123456789": // Example identifier for a corporate bond
                return 1002; // Return corresponding internal symbol ID
            default:
                throw new IllegalArgumentException("Unknown bond symbol: " + symbol);
        }
    }


    private void handleOrderStatusRequest(OrderStatusRequest request) throws FieldNotFound {
        String clOrdID = request.getClOrdID().getValue();
        long orderId = Long.parseLong(clOrdID);

        System.out.println("Processing OrderStatusRequest: clOrdID=" + clOrdID);

        exchangeApi.submitCommandAsyncFullResponse(ApiCancelOrder.builder()
                .orderId(orderId)
                .uid(301L)  // Example UID
                .symbol(1001)  // Example symbol ID
                .build()).thenAccept(orderStatus -> {
            // Construct an ExecutionReport based on the order status result
            ExecutionReport executionReport = new ExecutionReport(
                    new OrderID(Long.toString(orderStatus.orderId)),
                    new ExecID(Long.toString(orderStatus.orderId)),
                    new ExecType(orderStatus.size == 0 ? ExecType.FILL : ExecType.PARTIAL_FILL),
                    new OrdStatus(orderStatus.size == 0 ? OrdStatus.FILLED : OrdStatus.PARTIALLY_FILLED),
                    new Side(Side.BUY),  // Adjust logic for side
                    new LeavesQty(orderStatus.size),  // Adjust as per logic
                    new CumQty(orderStatus.size),  // Adjust as per logic
                    new AvgPx(orderStatus.price)  // Adjust as per logic
            );
            executionReport.set(new ClOrdID(clOrdID));
            executionReport.set(new LastPx(orderStatus.price));

            // Send the execution report back to the FIX client
            try {
                Session.sendToTarget(executionReport, currentSessionId);  // Use the stored sessionId
            } catch (SessionNotFound sessionNotFound) {
                sessionNotFound.printStackTrace();
            }
        });
    }




    private void handleMarketDataRequest(MarketDataRequest request) throws FieldNotFound {
        MarketDepth depthField = request.get(new MarketDepth());  // Retrieve market depth
        int depth = depthField.getValue();  // Extract depth value

        // NoRelatedSym is a repeating group, so we'll iterate over it
        MarketDataRequest.NoRelatedSym group = new MarketDataRequest.NoRelatedSym();

        for (int i = 1; i <= request.getGroupCount(NoRelatedSym.FIELD); i++) {
            request.getGroup(i, group);
            Symbol symbolField = group.getSymbol();  // Retrieve symbol from the group
            String symbol = symbolField.getValue();  // Extract symbol value

            // Convert the symbol string to your internal symbol ID
            long symbolId = getSymbolIdForBond(symbol);

            System.out.println("Processing MarketDataRequest for symbol: " + symbol);

            exchangeApi.requestOrderBookAsync((int) symbolId, depth)
                    .thenAccept(marketData -> {
                        // Construct and send a MarketDataSnapshotFullRefresh message
                        MarketDataSnapshotFullRefresh marketDataSnapshot = new MarketDataSnapshotFullRefresh();

                        // Adding bid prices
                        for (int j = 0; j < marketData.bidSize; j++) {
                            MarketDataSnapshotFullRefresh.NoMDEntries entry = new MarketDataSnapshotFullRefresh.NoMDEntries();
                            entry.set(new MDEntryType(MDEntryType.BID));
                            entry.set(new MDEntryPx(marketData.bidPrices[j]));
                            entry.set(new MDEntrySize(marketData.bidVolumes[j]));
                            marketDataSnapshot.addGroup(entry);
                        }

                        // Adding ask prices
                        for (int j = 0; j < marketData.askSize; j++) {
                            MarketDataSnapshotFullRefresh.NoMDEntries entry = new MarketDataSnapshotFullRefresh.NoMDEntries();
                            entry.set(new MDEntryType(MDEntryType.OFFER));
                            entry.set(new MDEntryPx(marketData.askPrices[j]));
                            entry.set(new MDEntrySize(marketData.askVolumes[j]));
                            marketDataSnapshot.addGroup(entry);
                        }

                        // Send the market data snapshot back to the FIX client
                        try {
                            Session.sendToTarget(marketDataSnapshot, currentSessionId);  // Use the stored sessionId
                        } catch (SessionNotFound sessionNotFound) {
                            sessionNotFound.printStackTrace();
                        }
                    });
        }
    }

}
