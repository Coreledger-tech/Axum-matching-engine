package exchange.core2.core;

import exchange.core2.core.common.config.ExchangeConfiguration;
import quickfix.*;

import java.io.InputStream;

public class FixEngine {
    public static void main(String[] args) throws Exception {
        try {
        // Load FIX session settings from the configuration file
        InputStream inputStream = FixEngine.class.getResourceAsStream("/quickfixj.cfg");
        //assert inputStream != null;
        SessionSettings settings = new SessionSettings(inputStream);

        // Create the FIX application
        Application application = new FixApp(getExchangeApi());

        // Set up the message store, log, and message factories
        MessageStoreFactory storeFactory = new FileStoreFactory(settings);
        LogFactory logFactory = new FileLogFactory(settings);
        MessageFactory messageFactory = new DefaultMessageFactory();

        // Initialize and start the SocketAcceptor
        Acceptor acceptor = new SocketAcceptor(application, storeFactory, settings, logFactory, messageFactory);
        acceptor.start();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to initialize the Exchange API for Axum
    private static ExchangeApi getExchangeApi() {
        SimpleEventsProcessor eventsProcessor = new SimpleEventsProcessor(new IEventsHandler() {
            @Override
            public void tradeEvent(TradeEvent tradeEvent) {
                System.out.println("Trade event: " + tradeEvent);
            }

            @Override
            public void reduceEvent(ReduceEvent reduceEvent) {
                System.out.println("Reduce event: " + reduceEvent);
            }

            @Override
            public void rejectEvent(RejectEvent rejectEvent) {
                System.out.println("Reject event: " + rejectEvent);
            }

            @Override
            public void commandResult(ApiCommandResult commandResult) {
                System.out.println("Command result: " + commandResult);
            }

            @Override
            public void orderBook(OrderBook orderBook) {
                System.out.println("OrderBook event: " + orderBook);
            }
        });

        ExchangeConfiguration config = ExchangeConfiguration.defaultBuilder().build();
        ExchangeCore exchangeCore = ExchangeCore.builder()
                .resultsConsumer(eventsProcessor)
                .exchangeConfiguration(config)
                .build();
        exchangeCore.startup();

        return exchangeCore.getApi();
    }
}