package exchange.core2.core;

import exchange.core2.core.common.CoreSymbolSpecification;
import exchange.core2.core.common.SymbolType;
import exchange.core2.core.common.api.binary.BatchAddSymbolsCommand;
import exchange.core2.core.common.cmd.CommandResultCode;
import exchange.core2.core.common.config.ExchangeConfiguration;
import lombok.Getter;

import java.util.concurrent.ExecutionException;

public class Axum {

    @Getter
    private static ExchangeApi api;

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
        api = exchangeCore.getApi();

        // Define and submit bond trading symbol
        final int bondSymbolId = 1001;
        CoreSymbolSpecification bondSymbol = CoreSymbolSpecification.builder()
                .symbolId(bondSymbolId)
                .type(SymbolType.CURRENCY_EXCHANGE_PAIR)
                .baseCurrency(1) // Base currency code
                .quoteCurrency(2) // Quote currency code
                .baseScaleK(1_000_000L)
                .quoteScaleK(10_000L)
                .takerFee(100L)
                .makerFee(50L)
                .build();

        api.submitBinaryDataAsync(new BatchAddSymbolsCommand(bondSymbol)).get();
    }

}
