package com.habib.tradesimulator.market;

import com.habib.tradesimulator.core.Stock;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * 🏛️ StockMarket - Central registry and manager for all stocks
 *
 * WHY: Need centralized place to manage all stocks, get current prices
 * MODERN FEATURES: ConcurrentHashMap for fast lookups, CopyOnWriteArrayList for iteration
 * THREAD SAFETY: Multiple traders + price updater access same stocks
 */
public class StockMarket {

    // 📋 Thread-safe collections for concurrent access
    private final Map<String, Stock> stockRegistry = new ConcurrentHashMap<>();
    private final List<Stock> stockList = new CopyOnWriteArrayList<>();  // Safe for iteration during updates

    private volatile boolean marketOpen = false;  // Market status

    /**
     * 📈 Register a new stock in the market
     */
    public void registerStock(Stock stock) {
        Objects.requireNonNull(stock, "Stock cannot be null");

        String symbol = stock.getSymbol();
        if (stockRegistry.containsKey(symbol)) {
            throw new IllegalArgumentException("Stock " + symbol + " already registered");
        }

        stockRegistry.put(symbol, stock);
        stockList.add(stock);

        System.out.printf("🏛️ Stock %s registered in market\n", symbol);
    }

    /**
     * 🔍 Get stock by symbol (thread-safe lookup)
     */
    public Optional<Stock> getStock(String symbol) {
        return Optional.ofNullable(stockRegistry.get(symbol));
    }

    /**
     * 📋 Get all registered stocks (returns defensive copy)
     */
    public List<Stock> getAllStocks() {
        return new ArrayList<>(stockList);  // Defensive copy to prevent external modification
    }

    /**
     * 🎲 Get random stock for trading decisions
     */
    public Stock getRandomStock() {
        if (stockList.isEmpty()) {
            throw new IllegalStateException("No stocks registered in market");
        }
        Random random = new Random();
        return stockList.get(random.nextInt(stockList.size()));
    }

    /**
     * 💰 Get current prices for all stocks (for portfolio valuation)
     */
    public Map<String, Double> getCurrentPrices() {
        return stockRegistry.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().getCurrentPrice()
                ));
    }

    /**
     * 🔔 Market control methods
     */
    public void openMarket() {
        marketOpen = true;
        System.out.println("🔔 📈 MARKET OPENED! Trading begins...");
    }

    public void closeMarket() {
        marketOpen = false;
        System.out.println("🔔 📉 MARKET CLOSED! Trading suspended...");
    }

    public boolean isMarketOpen() {
        return marketOpen;
    }

    /**
     * 📊 Market summary statistics
     */
    public void printMarketSummary() {
        System.out.println("\n🏛️ MARKET SUMMARY");
        System.out.println("=".repeat(50));
        System.out.printf("📊 Total Stocks: %d\n", stockList.size());
        System.out.printf("🔔 Market Status: %s\n", marketOpen ? "OPEN" : "CLOSED");

        if (!stockList.isEmpty()) {
            System.out.println("\n📈 Current Prices:");
            stockList.forEach(stock -> {
                double changePercent = stock.getPriceChangePercent();
                String trend = changePercent > 0 ? "📈" : changePercent < 0 ? "📉" : "➡️";
                System.out.printf("   %s %s: $%.2f (%.2f%%)\n",
                        trend, stock.getSymbol(), stock.getCurrentPrice(), changePercent);
            });
        }
        System.out.println("=".repeat(50));
    }

    /**
     * 🎯 Find stocks with biggest price movements
     */
    public void printTopMovers() {
        if (stockList.isEmpty()) return;

        System.out.println("\n🎯 TOP MARKET MOVERS:");

        // Sort by absolute price change percentage
        List<Stock> sortedStocks = stockList.stream()
                .sorted((s1, s2) -> Double.compare(
                        Math.abs(s2.getPriceChangePercent()),
                        Math.abs(s1.getPriceChangePercent())
                ))
                .collect(Collectors.toList());

        System.out.println("📈 Biggest Gainers/Losers:");
        sortedStocks.forEach(stock -> {
            double change = stock.getPriceChangePercent();
            String emoji = change > 5 ? "🚀" : change > 0 ? "📈" : change < -5 ? "💥" : "📉";
            System.out.printf("   %s %s: %.2f%%\n", emoji, stock.getSymbol(), change);
        });
    }
}