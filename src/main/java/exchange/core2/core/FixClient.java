package exchange.core2.core;

import quickfix.*;
import quickfix.MessageFactory;
import quickfix.field.*;
import quickfix.fix44.*;

import java.io.InputStream;

public class FixClient {
    public static void main(String[] args) {
        try {
            // Load FIX session settings
            InputStream inputStream = FixClient.class.getResourceAsStream("/fixclient.cfg");

            SessionSettings settings;
            if (inputStream == null) {
                throw new NullPointerException("FIX configuration file 'quickfixj.cfg' not found in the classpath");
            } else {
                settings = new SessionSettings(inputStream);
            }

            // Create FIX application
            Application application = new ApplicationAdapter();

            // Set up the message store, log, and message factories
            MessageStoreFactory storeFactory = new FileStoreFactory(settings);
            LogFactory logFactory = new FileLogFactory(settings);
            MessageFactory messageFactory = new DefaultMessageFactory();

            // Initialize and start the SocketInitiator
            Initiator initiator = new SocketInitiator(application, storeFactory, settings, logFactory, messageFactory);
            // Initialize and start the SocketInitiator
            initiator.start();

            // Ensure there is at least one session
            if (!initiator.getSessions().isEmpty()) {
                // Send a New Order - Buy
                sendNewOrderBuy(initiator.getSessions().get(0));

                // Send a New Order - Sell
                sendNewOrderSell(initiator.getSessions().get(0));

                // Send an Order Cancel Request
                sendOrderCancelRequest(initiator.getSessions().get(0));

                // Send a Market Data Request
                sendMarketDataRequest(initiator.getSessions().get(0));

                // Send a Request For Quote
                sendRequestForQuote(initiator.getSessions().get(0));
            } else {
                System.err.println("No sessions available. Ensure the connection is established.");
            }

// Stop the initiator
            initiator.stop();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendNewOrderBuy(SessionID sessionId) throws SessionNotFound {
        NewOrderSingle newOrderSingle = new NewOrderSingle(
                new ClOrdID("12345"),
                new Side(Side.BUY),
                new TransactTime(),
                new OrdType(OrdType.LIMIT)
        );
        newOrderSingle.set(new Symbol("AAPL"));
        newOrderSingle.set(new OrderQty(100));
        newOrderSingle.set(new Price(150.0));
        Session.sendToTarget(newOrderSingle, sessionId);
    }

    private static void sendNewOrderSell(SessionID sessionId) throws SessionNotFound {
        NewOrderSingle newOrderSingle = new NewOrderSingle(
                new ClOrdID("12346"),
                new Side(Side.SELL),
                new TransactTime(),
                new OrdType(OrdType.LIMIT)
        );
        newOrderSingle.set(new Symbol("AAPL"));
        newOrderSingle.set(new OrderQty(50));
        newOrderSingle.set(new Price(152.0));
        Session.sendToTarget(newOrderSingle, sessionId);
    }

    private static void sendOrderCancelRequest(SessionID sessionId) throws SessionNotFound {
        OrderCancelRequest cancelRequest = new OrderCancelRequest(
                new OrigClOrdID("12345"),
                new ClOrdID("12347"),
                new Side(Side.BUY),
                new TransactTime()
        );
        cancelRequest.set(new Symbol("AAPL"));
        Session.sendToTarget(cancelRequest, sessionId);
    }

    private static void sendMarketDataRequest(SessionID sessionId) throws SessionNotFound {
        MarketDataRequest marketDataRequest = new MarketDataRequest(
                new MDReqID("12348"),
                new SubscriptionRequestType(SubscriptionRequestType.SNAPSHOT),
                new MarketDepth(1)
        );
        MarketDataRequest.NoRelatedSym noRelatedSym = new MarketDataRequest.NoRelatedSym();
        noRelatedSym.set(new Symbol("AAPL"));
        marketDataRequest.addGroup(noRelatedSym);
        Session.sendToTarget(marketDataRequest, sessionId);
    }

    private static void sendRequestForQuote(SessionID sessionId) throws SessionNotFound {
        QuoteRequest quoteRequest = new QuoteRequest(new QuoteReqID("12349"));
        QuoteRequest.NoRelatedSym group = new QuoteRequest.NoRelatedSym();
        group.set(new Symbol("AAPL"));
        quoteRequest.addGroup(group);
        Session.sendToTarget(quoteRequest, sessionId);
    }
}
