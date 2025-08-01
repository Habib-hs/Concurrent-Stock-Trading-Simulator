package com.habib.tradesimulator.core;

import com.habib.tradesimulator.market.StockMarket;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 👤 Trader - Individual trading thread with realistic behavior
 *
 * WHY: Each trader is independent thread making autonomous decisions
 * FEATURES: Risk management, trading strategies, realistic timing
 * THREAD SAFETY: Accesses shared resources (stocks, market, trade counter)
 */
public class Trader implements Runnable {

    private final String name;
    private final Portfolio portfolio;
    private final StockMarket stockMarket;
    private final TradeCounter tradeCounter;

    // Trading behavior parameters
    private final double riskTolerance;      // 0.0 to 1.0 (how aggressive)
    private final int minWaitMs = 3000;      // Minimum 3 seconds between trades
    private final int maxWaitMs = 8000;      // Maximum 8 seconds between trades

    private volatile boolean active = true;   // Control flag for stopping trader

    public Trader(String name, double initialCash, StockMarket stockMarket, TradeCounter tradeCounter) {
        this.name = name;
        this.portfolio = new Portfolio(name, initialCash);
        this.stockMarket = stockMarket;
        this.tradeCounter = tradeCounter;
        this.riskTolerance = 0.3 + ThreadLocalRandom.current().nextDouble() * 0.4; // 0.3 to 0.7

        System.out.printf("👤 Trader %s joined - Risk tolerance: %.1f%%, Cash: $%.2f\n",
                name, riskTolerance * 100, initialCash);
    }

    @Override
    public void run() {
        System.out.printf("🚀 %s started trading!\n", name);

        try {
            while (active && stockMarket.isMarketOpen() && !Thread.currentThread().isInterrupted()) {

                // Wait before next trade (realistic human behavior)
                int waitTime = ThreadLocalRandom.current().nextInt(minWaitMs, maxWaitMs + 1);
                Thread.sleep(waitTime);

                // Make a trading decision
                makeSmartTradingDecision();

            }
        } catch (InterruptedException e) {
            System.out.printf("⚠️ %s was interrupted\n", name);
        } finally {
            System.out.printf("🏁 %s finished trading\n", name);
        }
    }

    /**
     * 🧠 Smart trading logic with multiple strategies
     */
    private void makeSmartTradingDecision() {
        // Get a random stock to consider
        Stock stock = stockMarket.getRandomStock();
        double currentPrice = stock.getCurrentPrice();
        double priceChange = stock.getPriceChangePercent();

        // Decide action based on multiple factors
        TradeAction action = analyzeMarketAndDecide(stock, currentPrice, priceChange);

        switch (action) {
            case BUY -> attemptBuy(stock, currentPrice, priceChange);
            case SELL -> attemptSell(stock, currentPrice);
            case HOLD -> {
                // Do nothing this round
                System.out.printf("💭 %s decided to HOLD on %s\n", name, stock.getSymbol());
            }
        }
    }

    /**
     * 📊 Market analysis and decision making
     */
    private TradeAction analyzeMarketAndDecide(Stock stock, double currentPrice, double priceChange) {
        double cashBalance = portfolio.getCashBalance();
        int currentHolding = portfolio.getStockQuantity(stock.getSymbol());

        // Strategy 1: Momentum Trading (follow the trend)
        if (priceChange > 2.0 && riskTolerance > 0.5) {
            return TradeAction.BUY;  // Price going up, jump on trend
        }

        // Strategy 2: Value Trading (buy the dip)
        if (priceChange < -3.0 && cashBalance > currentPrice * 10) {
            return TradeAction.BUY;  // Price dropped, potential bargain
        }

        // Strategy 3: Profit Taking
        if (currentHolding > 0 && priceChange > 4.0) {
            return TradeAction.SELL;  // Take profits when price is high
        }

        // Strategy 4: Risk Management
        if (currentHolding > 0 && priceChange < -5.0) {
            return TradeAction.SELL;  // Cut losses
        }

        // Strategy 5: Random trading (simulate market noise)
        double randomFactor = ThreadLocalRandom.current().nextDouble();
        if (randomFactor < 0.3) {
            return cashBalance > currentPrice * 5 ? TradeAction.BUY : TradeAction.HOLD;
        } else if (randomFactor < 0.5 && currentHolding > 0) {
            return TradeAction.SELL;
        }

        return TradeAction.HOLD;
    }

    private void attemptBuy(Stock stock, double currentPrice, double priceChange) {
        double cashBalance = portfolio.getCashBalance();

        // Calculate how much to buy based on risk tolerance and market conditions
        int maxAffordable = (int) (cashBalance / currentPrice);
        if (maxAffordable == 0) return;

        // Risk-based quantity calculation
        int baseQuantity = Math.min(5, maxAffordable);  // Base: 1-5 shares
        int quantity = Math.max(1, (int) (baseQuantity * riskTolerance));

        // Adjust for market conditions
        if (priceChange > 0) {
            quantity = Math.max(1, quantity / 2);  // Buy less when price is rising
        }

        boolean success = portfolio.buyStock(stock.getSymbol(), quantity, currentPrice);

        if (success) {
            tradeCounter.recordSuccessfulTrade();
        } else {
            tradeCounter.recordFailedTrade();
        }
    }

    private void attemptSell(Stock stock, double currentPrice) {
        int currentHolding = portfolio.getStockQuantity(stock.getSymbol());

        if (currentHolding == 0) {
            tradeCounter.recordFailedTrade();
            return;
        }

        // Decide how much to sell (partial or full)
        int quantityToSell = ThreadLocalRandom.current().nextBoolean() ?
                Math.min(currentHolding, ThreadLocalRandom.current().nextInt(1, 4)) : // Partial sale
                currentHolding; // Full sale

        boolean success = portfolio.sellStock(stock.getSymbol(), quantityToSell, currentPrice);

        if (success) {
            tradeCounter.recordSuccessfulTrade();
        } else {
            tradeCounter.recordFailedTrade();
        }
    }

    public void stopTrading() {
        active = false;
    }

    public void printFinalPortfolio() {
        Map<String, Double> currentPrices = stockMarket.getCurrentPrices();
        double totalValue = portfolio.calculateTotalValue(currentPrices);

        portfolio.printPortfolio();
        System.out.printf("💎 %s Total Portfolio Value: $%.2f\n", name, totalValue);
    }

    public String getName() { return name; }
    public Portfolio getPortfolio() { return portfolio; }

    // Trading decision enum
    private enum TradeAction {
        BUY, SELL, HOLD
    }
}