package exchange.core2.core;

import exchange.core2.core.common.CoreSymbolSpecification;
import exchange.core2.core.common.OrderAction;
import exchange.core2.core.common.OrderType;
import exchange.core2.core.common.SymbolType;
import exchange.core2.core.common.api.ApiAddUser;
import exchange.core2.core.common.api.ApiAdjustUserBalance;
import exchange.core2.core.common.api.ApiPlaceOrder;
import exchange.core2.core.common.api.binary.BatchAddSymbolsCommand;
import exchange.core2.core.common.cmd.CommandResultCode;
import exchange.core2.core.common.config.ExchangeConfiguration;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Axum {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
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

        // Configure and start ExchangeCore
        ExchangeConfiguration config = ExchangeConfiguration.defaultBuilder().build();
        ExchangeCore exchangeCore = ExchangeCore.builder()
                .resultsConsumer(eventsProcessor)
                .exchangeConfiguration(config)
                .build();
        exchangeCore.startup();

        // Obtain Exchange API
        ExchangeApi api = exchangeCore.getApi();

        // Define bond trading symbol
        final int bondSymbolId = 1001;
        CoreSymbolSpecification bondSymbol = CoreSymbolSpecification.builder()
                .symbolId(bondSymbolId)
                .type(SymbolType.CURRENCY_EXCHANGE_PAIR)
                .baseCurrency(1) // Assuming 1 is the currency code for base currency
                .quoteCurrency(2) // Assuming 2 is the currency code for quote currency
                .baseScaleK(1_000_000L)
                .quoteScaleK(10_000L)
                .takerFee(100L)
                .makerFee(50L)
                .build();

        // Submit bond symbol to the exchange
        api.submitBinaryDataAsync(new BatchAddSymbolsCommand(bondSymbol)).get();

        // Create user accounts
        Future<CommandResultCode> future1 = api.submitCommandAsync(ApiAddUser.builder().uid(301L).build());
        Future<CommandResultCode> future2 = api.submitCommandAsync(ApiAddUser.builder().uid(302L).build());
        future1.get();
        future2.get();

        // Perform deposits
        api.submitCommandAsync(ApiAdjustUserBalance.builder()
                .uid(301L)
                .currency(2)
                .amount(1_000_000_000L)
                .transactionId(1L)
                .build()).get();

        api.submitCommandAsync(ApiAdjustUserBalance.builder()
                .uid(302L)
                .currency(1)
                .amount(1_000_000L)
                .transactionId(2L)
                .build()).get();

        // Place orders
        api.submitCommandAsync(ApiPlaceOrder.builder()
                .uid(301L)
                .orderId(1L)
                .symbol(bondSymbolId)
                .price(150_000L)
                .size(1L)
                .action(OrderAction.BID)
                .orderType(OrderType.GTC)
                .build()).get();

        api.submitCommandAsync(ApiPlaceOrder.builder()
                .uid(302L)
                .orderId(2L)
                .symbol(bondSymbolId)
                .price(150_000L)
                .size(1L)
                .action(OrderAction.ASK)
                .orderType(OrderType.GTC)
                .build()).get();
    }
}





