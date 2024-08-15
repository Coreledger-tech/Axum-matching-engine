package exchange.core2.core;

import exchange.core2.core.common.CoreSymbolSpecification;
import exchange.core2.core.common.SymbolType;
import exchange.core2.core.common.api.ApiBinaryDataCommand;
import exchange.core2.core.common.api.binary.BatchAddSymbolsCommand;
import exchange.core2.core.common.cmd.CommandResultCode;
import exchange.core2.core.common.config.ExchangeConfiguration;
import quickfix.*;

import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class FixEngine {
    public static void main(String[] args) throws Exception {
        try {
            // Load FIX session settings from the configuration file
            InputStream inputStream = FixEngine.class.getResourceAsStream("/quickfix.cfg");

            SessionSettings settings;
            if (inputStream == null) {
                throw new NullPointerException("FIX configuration file 'quickfixj.cfg' not found in the classpath");
            } else {
                settings = new SessionSettings(inputStream);
            }

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
        ExchangeApi api = exchangeCore.getApi();

        // Define and add bond symbols
        try {
            addBondSymbols(api);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        return exchangeCore.getApi();
    }

    // Method to add bond symbols to the exchange
    private static void addBondSymbols(ExchangeApi api) throws ExecutionException, InterruptedException {
        CoreSymbolSpecification corporateBond = CoreSymbolSpecification.builder()
                .symbolId(1002)
                .type(SymbolType.BOND)
                .baseCurrency(840) // USD
                .quoteCurrency(840) // USD as quote
                .baseScaleK(1000L)
                .quoteScaleK(100L)
                .takerFee(0L)
                .makerFee(0L)
                .build();

        CoreSymbolSpecification governmentBond = CoreSymbolSpecification.builder()
                .symbolId(1003)
                .type(SymbolType.BOND)
                .baseCurrency(840) // USD
                .quoteCurrency(840) // USD as quote
                .baseScaleK(1000L)
                .quoteScaleK(100L)
                .takerFee(0L)
                .makerFee(0L)
                .build();

        // Wrap the bond symbols in an ApiBinaryDataCommand and submit them
        api.submitCommandAsync(new ApiBinaryDataCommand(1, new BatchAddSymbolsCommand(Arrays.asList(corporateBond, governmentBond))))
                .thenAccept(result -> {
                    if (result == CommandResultCode.SUCCESS) {
                        System.out.println("Bond symbols added successfully.");
                    } else {
                        System.err.println("Failed to add bond symbols.");
                    }
                }).get();
    }
}
