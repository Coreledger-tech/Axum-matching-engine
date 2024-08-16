package exchange.core2.core;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import quickfix.*;
import quickfix.field.*;
import quickfix.fix44.MarketDataRequest;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelRequest;
import quickfix.fix44.QuoteRequest;

import java.io.InputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ExpandedFixMessageTest {

    @Test
    public void testSequenceOfOrders() throws Exception {
        runFixClientTest(sessionId -> {
            // Send a buy order
            NewOrderSingle buyOrder = sendNewOrderBuy(sessionId);
            System.out.println("Sent NewOrderSingle (Buy): " + buyOrder);

            // Send a sell order
            NewOrderSingle sellOrder = sendNewOrderSell(sessionId);
            System.out.println("Sent NewOrderSingle (Sell): " + sellOrder);

            // Send an order cancel request
            OrderCancelRequest cancelRequest = sendOrderCancelRequest(sessionId);
            System.out.println("Sent OrderCancelRequest: " + cancelRequest);

            // Send a market data request
            MarketDataRequest marketDataRequest = sendMarketDataRequest(sessionId);
            System.out.println("Sent MarketDataRequest: " + marketDataRequest);
        });
    }

    @Test
    public void testNewOrderBuyWithMockVerification() throws Exception {
        // Create a mock Application
        Application mockApplication = Mockito.mock(Application.class);

        runFixClientTestWithMock(mockApplication, sessionId -> {
            // Send a new buy order
            NewOrderSingle buyOrder = sendNewOrderBuy(sessionId);
            System.out.println("Sent NewOrderSingle (Buy): " + buyOrder);

            // Verify that the `toApp` method was called, as this is invoked when sending a message
            verify(mockApplication, times(1)).toApp(any(Message.class), any(SessionID.class));
        });
    }



    @Test
    public void testHandlingMultipleTradeEvents() throws Exception {
        runFixClientTest(sessionId -> {
            // Send multiple trade events in sequence to simulate a trade flow
            NewOrderSingle firstTrade = sendNewOrderBuy(sessionId);
            NewOrderSingle secondTrade = sendNewOrderSell(sessionId);

            System.out.println("First Trade Sent: " + firstTrade);
            System.out.println("Second Trade Sent: " + secondTrade);
        });
    }

    private void runFixClientTest(FixMessageSender sender) throws Exception {
        // Load FIX session settings
        InputStream inputStream = ExpandedFixMessageTest.class.getResourceAsStream("/fixclient.cfg");

        SessionSettings settings;
        if (inputStream == null) {
            throw new NullPointerException("FIX configuration file 'fixclient.cfg' not found in the classpath");
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

    private void runFixClientTestWithMock(Application mockApplication, FixMessageSender sender) throws Exception {
        // Load FIX session settings
        InputStream inputStream = FixMessageTest.class.getResourceAsStream("/fixclient.cfg");

        SessionSettings settings;
        if (inputStream == null) {
            throw new NullPointerException("FIX configuration file 'fixclient.cfg' not found in the classpath");
        } else {
            settings = new SessionSettings(inputStream);
        }

        // Set up the message store, log, and message factories
        MessageStoreFactory storeFactory = new FileStoreFactory(settings);
        LogFactory logFactory = new FileLogFactory(settings);
        MessageFactory messageFactory = new DefaultMessageFactory();

        // Initialize and start the SocketInitiator
        Initiator initiator = new SocketInitiator(mockApplication, storeFactory, settings, logFactory, messageFactory);
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
        void send(SessionID sessionId) throws SessionNotFound, DoNotSend;
    }
}
