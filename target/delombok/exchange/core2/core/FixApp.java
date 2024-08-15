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

     private void handleOrderStatusRequest(OrderStatusRequest request) throws FieldNotFound {
        String clOrdID = request.getClOrdID().getValue();
        long orderId = Long.parseLong(clOrdID);

        System.out.println("Processing OrderStatusRequest: clOrdID=" + clOrdID);

//        exchangeApi.submitCommandAsyncFullResponse(ApiCancelOrder.builder()
//                .orderId(orderId)
//                .uid(301L) // Example UID
//                .symbol(1001) // Example symbol ID
//                        .build());
//                .build()).thenAccept(cmd -> {
//                    // Construct an ExecutionReport based on the command result
//                    ExecutionReport executionReport = new ExecutionReport(
//                            new OrderID(Long.toString(cmd.orderId)),
//                            new ExecID(Long.toString(cmd.orderId)),
//                            new ExecType(ExecType.FILL),
//                            new OrdStatus(OrdStatus.FILLED),
//                            new Symbol("US9128285M80"),
//                            new Side(Side.BUY),
//                            new LeavesQty(cmd.size),
//                            new CumQty(cmd.size),
//                            new AvgPx(cmd.price)
//                    );
//                    executionReport.set(new ClOrdID(clOrdID));
//                    executionReport.set(new LastShares(cmd.size));
//                    executionReport.set(new LastPx(cmd.price));
//
//                    // Send the execution report back to the FIX client
//                    try {
//                        Session.sendToTarget(executionReport, sessionId);
//                    } catch (SessionNotFound sessionNotFound) {
//                        sessionNotFound.printStackTrace();
//                    }
//                });
    }

    private void handleMarketDataRequest(MarketDataRequest request) throws FieldNotFound {
        String symbol = request.getScope().getValue();
        int depth = 10; // Example depth, could be adjusted based on the request

        System.out.println("Processing MarketDataRequest: " + request.toString());
//
//        exchangeApi.requestOrderBookAsync(1001, depth) // Example symbol ID
//                .thenAccept(marketData -> {
//                    // Construct and send a MarketDataSnapshotFullRefresh message
//                    MarketDataSnapshotFullRefresh marketDataSnapshot = new MarketDataSnapshotFullRefresh();
                    
                    // Adding bid prices
//                    for (int i = 0; i < marketData.getBids().size(); i++) {
//                        MarketDataSnapshotFullRefresh.NoMDEntries entry = new MarketDataSnapshotFullRefresh.NoMDEntries();
//                        entry.set(new MDEntryType(MDEntryType.BID));
//                        entry.set(new MDEntryPx(marketData.getBids().get(i).getPrice()));
//                        entry.set(new MDEntrySize(marketData.getBids().get(i).getVolume()));
//                        marketDataSnapshot.addGroup(entry);
//                    }
//
//                    // Adding ask prices
//                    for (int i = 0; i < marketData.getAsks().size(); i++) {
//                        MarketDataSnapshotFullRefresh.NoMDEntries entry = new MarketDataSnapshotFullRefresh.NoMDEntries();
//                        entry.set(new MDEntryType(MDEntryType.OFFER));
//                        entry.set(new MDEntryPx(marketData.getAsks().get(i).getPrice()));
//                        entry.set(new MDEntrySize(marketData.getAsks().get(i).getVolume()));
//                        marketDataSnapshot.addGroup(entry);
//                    }
//
//                    // Send the market data snapshot back to the FIX client
//                    try {
//                        Session.sendToTarget(marketDataSnapshot, sessionId);
//                    } catch (SessionNotFound sessionNotFound) {
//                        sessionNotFound.printStackTrace();
//                    }
 //               });
    }
}
