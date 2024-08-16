package exchange.core2.core;
import exchange.core2.core.common.*;
import exchange.core2.core.common.api.ApiAddUser;
import exchange.core2.core.common.api.ApiAdjustUserBalance;
import exchange.core2.core.common.api.ApiPlaceOrder;
import exchange.core2.core.common.api.binary.BatchAddSymbolsCommand;
import exchange.core2.core.common.api.reports.SingleUserReportQuery;
import exchange.core2.core.common.api.reports.SingleUserReportResult;
import exchange.core2.core.common.api.reports.TotalCurrencyBalanceReportQuery;
import exchange.core2.core.common.api.reports.TotalCurrencyBalanceReportResult;
import exchange.core2.core.common.cmd.CommandResultCode;
import exchange.core2.core.common.config.ExchangeConfiguration;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import quickfix.*;
import quickfix.field.*;
import quickfix.fix44.MarketDataRequest;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelRequest;
import quickfix.fix44.QuoteRequest;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

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

    @Test
    public void testPostTradeData() throws Exception {
        // Initialize events processor and exchange core as shown in ITCoreExample

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

        ExchangeCore exchangeCore = ExchangeCore.builder()
                .resultsConsumer(eventsProcessor)
                .exchangeConfiguration(ExchangeConfiguration.defaultBuilder().build())
                .build();

        exchangeCore.startup();

        ExchangeApi api = exchangeCore.getApi();
        int currencyCodeXbt = 11;
        int currencyCodeLtc = 15;
        int symbolXbtLtc = 241;

        // Add symbols, users, and balances (similar to ITCoreExample)

        // Place orders and trade

        // Get the post-trade data
        CompletableFuture<L2MarketData> orderBookFuture = api.requestOrderBookAsync(symbolXbtLtc, 10);
        L2MarketData orderBookData = orderBookFuture.get();
        System.out.println("Order book after trades: " + orderBookData);

        CompletableFuture<SingleUserReportResult> report1 = api.processReport(new SingleUserReportQuery(301), 0);
        System.out.println("User 301 balances: " + report1.get().getAccounts());

        CompletableFuture<SingleUserReportResult> report2 = api.processReport(new SingleUserReportQuery(302), 0);
        System.out.println("User 302 balances: " + report2.get().getAccounts());

        CompletableFuture<TotalCurrencyBalanceReportResult> totalsReport = api.processReport(new TotalCurrencyBalanceReportQuery(), 0);
        System.out.println("LTC fees collected: " + totalsReport.get().getFees().get(currencyCodeLtc));

        // Shutdown exchange core after test
        exchangeCore.shutdown();
    }

    @Test
    public void testBondTradeScenario() throws Exception {
        // Initialize events processor and exchange core

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

        ExchangeCore exchangeCore = ExchangeCore.builder()
                .resultsConsumer(eventsProcessor)
                .exchangeConfiguration(ExchangeConfiguration.defaultBuilder().build())
                .build();

        exchangeCore.startup();

        ExchangeApi api = exchangeCore.getApi();
        int currencyCodeUsd = 840; // USD
        int currencyCodeBonds = 1001; // Custom bond currency code
        int symbolBondTrade = 5001; // Symbol for bond trades

        // Add a bond symbol
        CoreSymbolSpecification bondSymbolSpec = CoreSymbolSpecification.builder()
                .symbolId(symbolBondTrade)
                .type(SymbolType.BOND)
                .baseCurrency(currencyCodeBonds)
                .quoteCurrency(currencyCodeUsd)
                .baseScaleK(1000L)
                .quoteScaleK(100L)
                .takerFee(500L)
                .makerFee(200L)
                .build();

        CompletableFuture<CommandResultCode> future = api.submitBinaryDataAsync(new BatchAddSymbolsCommand(bondSymbolSpec));
        System.out.println("BatchAddSymbolsCommand result: " + future.get());

        // Create users
        future = api.submitCommandAsync(ApiAddUser.builder().uid(1001L).build());
        System.out.println("ApiAddUser 1 result: " + future.get());

        future = api.submitCommandAsync(ApiAddUser.builder().uid(1002L).build());
        System.out.println("ApiAddUser 2 result: " + future.get());

        // User 1001 deposits USD
        future = api.submitCommandAsync(ApiAdjustUserBalance.builder()
                .uid(1001L)
                .currency(currencyCodeUsd)
                .amount(200_000_000L) // $1,000,000
                .transactionId(1L)
                .build());

        System.out.println("ApiAdjustUserBalance 1 result: " + future.get());

        // User 1002 deposits bonds
        future = api.submitCommandAsync(ApiAdjustUserBalance.builder()
                .uid(1002L)
                .currency(currencyCodeBonds)
                .amount(500_000L) // 500 bonds
                .transactionId(2L)
                .build());

        System.out.println("ApiAdjustUserBalance 2 result: " + future.get());

        // User 1001 places a Good-till-Cancel Bid order to buy bonds
        future = api.submitCommandAsync(ApiPlaceOrder.builder()
                .uid(1001L)
                .orderId(3001L)
                .price(102L) // Price of $102 per bond
                .size(200L) // 200 bonds
                .action(OrderAction.BID)
                .orderType(OrderType.GTC)
                .symbol(symbolBondTrade)
                .build());

        System.out.println("ApiPlaceOrder 1 result: " + future.get());

        // User 1002 places an Immediate-or-Cancel Ask order to sell bonds
        future = api.submitCommandAsync(ApiPlaceOrder.builder()
                .uid(1002L)
                .orderId(3002L)
                .price(101L) // Price of $101 per bond
                .size(150L) // 150 bonds
                .action(OrderAction.ASK)
                .orderType(OrderType.IOC)
                .symbol(symbolBondTrade)
                .build());

        System.out.println("ApiPlaceOrder 2 result: " + future.get());

        // Request order book
        CompletableFuture<L2MarketData> orderBookFuture = api.requestOrderBookAsync(symbolBondTrade, 10);
        L2MarketData orderBookData = orderBookFuture.get();
        System.out.println("Order book after trades: " + orderBookData);

        // Get user balance reports
        CompletableFuture<SingleUserReportResult> report1 = api.processReport(new SingleUserReportQuery(1001), 0);
        System.out.println("User 1001 balances: " + report1.get().getAccounts());

        CompletableFuture<SingleUserReportResult> report2 = api.processReport(new SingleUserReportQuery(1002), 0);
        System.out.println("User 1002 balances: " + report2.get().getAccounts());

        // Check fees collected
        CompletableFuture<TotalCurrencyBalanceReportResult> totalsReport = api.processReport(new TotalCurrencyBalanceReportQuery(), 0);
        System.out.println("USD fees collected: " + totalsReport.get().getFees().get(currencyCodeUsd));

        // Shutdown exchange core after test
        exchangeCore.shutdown();
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
