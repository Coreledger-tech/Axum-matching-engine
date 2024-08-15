package exchange.core2.core;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import quickfix.*;
import quickfix.MessageFactory;
import quickfix.field.*;
import quickfix.fix44.*;

import java.io.InputStream;

public class FixEngineTest {

    @Test
    public void testNewOrderBuy() throws Exception {
        runFixClientTest(sessionId -> {
            NewOrderSingle newOrderSingle = sendNewOrderBuy(sessionId);
            System.out.println("Sent NewOrderSingle (Buy): " + newOrderSingle);
        });
    }

    @Test
    public void testNewOrderSell() throws Exception {
        runFixClientTest(sessionId -> {
            NewOrderSingle newOrderSingle = sendNewOrderSell(sessionId);
            System.out.println("Sent NewOrderSingle (Sell): " + newOrderSingle);
        });
    }

    @Test
    public void testOrderCancelRequest() throws Exception {
        runFixClientTest(sessionId -> {
            OrderCancelRequest cancelRequest = sendOrderCancelRequest(sessionId);
            System.out.println("Sent OrderCancelRequest: " + cancelRequest);
        });
    }

    @Test
    public void testMarketDataRequest() throws Exception {
        runFixClientTest(sessionId -> {
            MarketDataRequest marketDataRequest = sendMarketDataRequest(sessionId);
            System.out.println("Sent MarketDataRequest: " + marketDataRequest);
        });
    }

    @Test
    public void testRequestForQuote() throws Exception {
        runFixClientTest(sessionId -> {
            QuoteRequest quoteRequest = sendRequestForQuote(sessionId);
            System.out.println("Sent RequestForQuote: " + quoteRequest);
        });
    }

    private void runFixClientTest(FixMessageSender sender) throws Exception {
        // Load FIX session settings
        InputStream inputStream = FixEngineTest.class.getResourceAsStream("/fixclient.cfg");

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
        initiator.start();

        // Send the FIX message using the provided sender
        sender.send(initiator.getSessions().get(0));

        // Stop the initiator
        initiator.stop();
    }

    private NewOrderSingle sendNewOrderBuy(SessionID sessionId) throws SessionNotFound {
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
        return newOrderSingle;
    }

    private NewOrderSingle sendNewOrderSell(SessionID sessionId) throws SessionNotFound {
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
        return newOrderSingle;
    }

    private OrderCancelRequest sendOrderCancelRequest(SessionID sessionId) throws SessionNotFound {
        OrderCancelRequest cancelRequest = new OrderCancelRequest(
                new OrigClOrdID("12345"),
                new ClOrdID("12347"),
                new Side(Side.BUY),
                new TransactTime()
        );
        cancelRequest.set(new Symbol("AAPL"));
        Session.sendToTarget(cancelRequest, sessionId);
        return cancelRequest;
    }

    private MarketDataRequest sendMarketDataRequest(SessionID sessionId) throws SessionNotFound {
        MarketDataRequest marketDataRequest = new MarketDataRequest(
                new MDReqID("12348"),
                new SubscriptionRequestType(SubscriptionRequestType.SNAPSHOT),
                new MarketDepth(1)
        );
        MarketDataRequest.NoRelatedSym noRelatedSym = new MarketDataRequest.NoRelatedSym();
        noRelatedSym.set(new Symbol("AAPL"));
        marketDataRequest.addGroup(noRelatedSym);
        Session.sendToTarget(marketDataRequest, sessionId);
        return marketDataRequest;
    }

    private QuoteRequest sendRequestForQuote(SessionID sessionId) throws SessionNotFound {
        QuoteRequest quoteRequest = new QuoteRequest(new QuoteReqID("12349"));
        QuoteRequest.NoRelatedSym group = new QuoteRequest.NoRelatedSym();
        group.set(new Symbol("AAPL"));
        quoteRequest.addGroup(group);
        Session.sendToTarget(quoteRequest, sessionId);
        return quoteRequest;
    }

    @FunctionalInterface
    interface FixMessageSender {
        void send(SessionID sessionId) throws SessionNotFound;
    }
}
