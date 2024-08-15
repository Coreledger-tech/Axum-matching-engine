//package exchange.core2.core;
//
//import org.junit.jupiter.api.Test;
//import quickfix.*;
//
//import java.io.InputStream;
//
//public class FixMessageTest {
//
//    @Test
//    public void testNewOrderBuy() throws Exception {
//        runFixClientTest(sessionId -> {
//            String fixMessage = "8=FIX.4.4|9=112|35=D|49=AXUM_ENGINE|56=TEST_COUNTERPARTY|11=12345|54=1|55=AAPL|38=100|40=2|44=150.00|60=20230718-15:01:30.123|10=128|";
//            Message message = new Message(fixMessage.replace("|", "\u0001"));
//            Session.sendToTarget(message, sessionId);
//        });
//    }
//
//    @Test
//    public void testNewOrderSell() throws Exception {
//        runFixClientTest(sessionId -> {
//            String fixMessage = "8=FIX.4.4|9=112|35=D|49=AXUM_ENGINE|56=TEST_COUNTERPARTY|11=12346|54=2|55=AAPL|38=50|40=2|44=152.00|60=20230718-15:01:30.123|10=129|";
//            Message message = new Message(fixMessage.replace("|", "\u0001"));
//            Session.sendToTarget(message, sessionId);
//        });
//    }
//
//    @Test
//    public void testOrderCancelRequest() throws Exception {
//        runFixClientTest(sessionId -> {
//            String fixMessage = "8=FIX.4.4|9=96|35=F|49=AXUM_ENGINE|56=TEST_COUNTERPARTY|41=12345|11=12347|54=1|55=AAPL|60=20230718-15:01:30.123|10=130|";
//            Message message = new Message(fixMessage.replace("|", "\u0001"));
//            Session.sendToTarget(message, sessionId);
//        });
//    }
//
//    @Test
//    public void testMarketDataRequest() throws Exception {
//        runFixClientTest(sessionId -> {
//            String fixMessage = "8=FIX.4.4|9=98|35=V|49=AXUM_ENGINE|56=TEST_COUNTERPARTY|262=12348|263=1|264=1|146=1|55=AAPL|10=131|";
//            Message message = new Message(fixMessage.replace("|", "\u0001"));
//            Session.sendToTarget(message, sessionId);
//        });
//    }
//
//    @Test
//    public void testRequestForQuote() throws Exception {
//        runFixClientTest(sessionId -> {
//            String fixMessage = "8=FIX.4.4|9=83|35=R|49=AXUM_ENGINE|56=TEST_COUNTERPARTY|131=12349|146=1|55=AAPL|10=132|";
//            Message message = new Message(fixMessage.replace("|", "\u0001"));
//            Session.sendToTarget(message, sessionId);
//        });
//    }
//
//    private void runFixClientTest(FixMessageSender sender) throws Exception {
//        // Load FIX session settings
//        InputStream inputStream = FixMessageTest.class.getResourceAsStream("/fixclient.cfg");
//
//        SessionSettings settings;
//        if (inputStream == null) {
//            throw new NullPointerException("FIX configuration file 'quickfixj.cfg' not found in the classpath");
//        } else {
//            settings = new SessionSettings(inputStream);
//        }
//
//        // Create FIX application
//        Application application = new ApplicationAdapter();
//
//        // Set up the message store, log, and message factories
//        MessageStoreFactory storeFactory = new FileStoreFactory(settings);
//        LogFactory logFactory = new FileLogFactory(settings);
//        MessageFactory messageFactory = new DefaultMessageFactory();
//
//        // Initialize and start the SocketInitiator
//        Initiator initiator = new SocketInitiator(application, storeFactory, settings, logFactory, messageFactory);
//        initiator.start();
//
//        // Send the FIX message using the provided sender
//        sender.send(initiator.getSessions().get(0));
//
//        // Stop the initiator
//        initiator.stop();
//    }
//
//
//    @FunctionalInterface
//    interface FixMessageSender {
//        void send(SessionID sessionId) throws SessionNotFound, InvalidMessage;
//    }
//}


package exchange.core2.core;

import org.junit.jupiter.api.Test;
import quickfix.*;
import quickfix.field.*;
import quickfix.fix44.MarketDataRequest;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelRequest;
import quickfix.fix44.QuoteRequest;

import java.io.InputStream;

public class FixMessageTest {

    @Test
    public void testNewOrderBuy() throws Exception {
        runFixClientTest(sessionId -> {
            NewOrderSingle newOrderSingle = new NewOrderSingle(
                    new ClOrdID("12345"),
                    new Side(Side.BUY),
                    new TransactTime(),
                    new OrdType(OrdType.LIMIT)
            );
            newOrderSingle.set(new Symbol("AAPL"));
            newOrderSingle.set(new OrderQty(100));
            newOrderSingle.set(new Price(150.0));
            System.out.println("Sending NewOrderSingle (Buy): " + newOrderSingle);
            Session.sendToTarget(newOrderSingle, sessionId);
        });
    }

    @Test
    public void testNewOrderSell() throws Exception {
        runFixClientTest(sessionId -> {
            NewOrderSingle newOrderSingle = new NewOrderSingle(
                    new ClOrdID("12346"),
                    new Side(Side.SELL),
                    new TransactTime(),
                    new OrdType(OrdType.LIMIT)
            );
            newOrderSingle.set(new Symbol("AAPL"));
            newOrderSingle.set(new OrderQty(50));
            newOrderSingle.set(new Price(152.0));
            System.out.println("Sending NewOrderSingle (Sell): " + newOrderSingle);
            Session.sendToTarget(newOrderSingle, sessionId);
        });
    }

    @Test
    public void testOrderCancelRequest() throws Exception {
        runFixClientTest(sessionId -> {
            OrderCancelRequest cancelRequest = new OrderCancelRequest(
                    new OrigClOrdID("12345"),
                    new ClOrdID("12347"),
                    new Side(Side.BUY),
                    new TransactTime()
            );
            cancelRequest.set(new Symbol("AAPL"));
            System.out.println("Sending OrderCancelRequest: " + cancelRequest);
            Session.sendToTarget(cancelRequest, sessionId);
        });
    }

    @Test
    public void testMarketDataRequest() throws Exception {
        runFixClientTest(sessionId -> {
            MarketDataRequest marketDataRequest = new MarketDataRequest(
                    new MDReqID("12348"),
                    new SubscriptionRequestType(SubscriptionRequestType.SNAPSHOT),
                    new MarketDepth(1)
            );
            MarketDataRequest.NoRelatedSym noRelatedSym = new MarketDataRequest.NoRelatedSym();
            noRelatedSym.set(new Symbol("AAPL"));
            marketDataRequest.addGroup(noRelatedSym);
            System.out.println("Sending MarketDataRequest: " + marketDataRequest);
            Session.sendToTarget(marketDataRequest, sessionId);
        });
    }

    @Test
    public void testRequestForQuote() throws Exception {
        runFixClientTest(sessionId -> {
            QuoteRequest quoteRequest = new QuoteRequest(new QuoteReqID("12349"));
            QuoteRequest.NoRelatedSym group = new QuoteRequest.NoRelatedSym();
            group.set(new Symbol("AAPL"));
            quoteRequest.addGroup(group);
            System.out.println("Sending RequestForQuote: " + quoteRequest);
            Session.sendToTarget(quoteRequest, sessionId);
        });
    }

    private void runFixClientTest(FixMessageSender sender) throws Exception {
        // Load FIX session settings
        InputStream inputStream = FixMessageTest.class.getResourceAsStream("/fixclient.cfg");

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

    @FunctionalInterface
    interface FixMessageSender {
        void send(SessionID sessionId) throws SessionNotFound;
    }
}
