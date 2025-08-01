package com.habib.tradesimulator.market;

import com.habib.tradesimulator.core.Stock;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * ⏰ PriceUpdater - Background thread that simulates real market price movements
 *
 * WHY: Real stock prices change constantly due to market forces
 * WHAT: Runs every 10 seconds, randomly updates stock prices
 * THREAD SAFETY: Accesses shared Stock objects that traders also use
 */
public class PriceUpdater extends Thread {

    private final StockMarket stockMarket;
    private final int updateIntervalMs;     // How often to update prices
    private final double maxPriceChange;    // Maximum price change percentage
    private volatile boolean running = true;  // Control flag for stopping

    /**
     * 🏗️ Constructor with customizable parameters
     */
    public PriceUpdater(StockMarket stockMarket, int updateIntervalMs, double maxPriceChangePercent) {
        super("PriceUpdater-Thread");  // Give thread a meaningful name
        this.stockMarket = stockMarket;
        this.updateIntervalMs = updateIntervalMs;
        this.maxPriceChange = maxPriceChangePercent / 100.0;  // Convert percentage to decimal

        System.out.printf("⏰ PriceUpdater created - Updates every %ds, max change ±%.1f%%\n",
                updateIntervalMs/1000, maxPriceChangePercent);
    }

    /**
     * 🔄 Default constructor for our simulation
     */
    public PriceUpdater(StockMarket stockMarket) {
        this(stockMarket, 10000, 5.0);  // Update every 10 seconds, ±5% max change
    }

    /**
     * 🎯 Main thread execution - runs until stopped
     */
    @Override
    public void run() {
        System.out.println("⏰ PriceUpdater started - Market price simulation begins!");

        try {
            while (running && !Thread.currentThread().isInterrupted()) {
                // Only update prices when market is open
                if (stockMarket.isMarketOpen()) {
                    updateRandomStockPrice();
                }

                // Wait for next update cycle
                Thread.sleep(updateIntervalMs);
            }
        } catch (InterruptedException e) {
            System.out.println("⏰ PriceUpdater interrupted - Stopping price updates");
        } finally {
            System.out.println("⏰ PriceUpdater stopped");
        }
    }

    /**
     * 📈 Update a random stock's price with realistic market simulation
     */
    private void updateRandomStockPrice() {
        List<Stock> stocks = stockMarket.getAllStocks();

        if (stocks.isEmpty()) {
            return;  // No stocks to update
        }

        // Pick a random stock
        Stock selectedStock = stocks.get(ThreadLocalRandom.current().nextInt(stocks.size()));

        // Generate realistic price movement
        double currentPrice = selectedStock.getCurrentPrice();
        double priceChange = generateRealisticPriceChange(currentPrice);
        double newPrice = currentPrice * (1 + priceChange);

        // Ensure price doesn't go negative
        newPrice = Math.max(newPrice, 0.01);

        // Update the stock price (Stock class handles thread safety)
        selectedStock.updatePrice(newPrice);

        // Simulate market news events occasionally
        if (ThreadLocalRandom.current().nextDouble() < 0.1) {  // 10% chance
            simulateMarketEvent(selectedStock, priceChange);
        }
    }

    /**
     * 📊 Generate realistic price movements using weighted randomness
     */
    private double generateRealisticPriceChange(double currentPrice) {
        Random random = ThreadLocalRandom.current();

        // 70% chance of small movements (-1% to +1%)
        // 20% chance of medium movements (-3% to +3%)
        // 10% chance of large movements (-5% to +5%)

        double probability = random.nextDouble();
        double changeRange;

        if (probability < 0.7) {
            // Small movement
            changeRange = 0.01;  // ±1%
        } else if (probability < 0.9) {
            // Medium movement
            changeRange = 0.03;  // ±3%
        } else {
            // Large movement
            changeRange = maxPriceChange;  // ±5%
        }

        // Generate random change within range
        return (random.nextDouble() - 0.5) * 2 * changeRange;  // -changeRange to +changeRange
    }

    /**
     * 📰 Simulate market news events for more realistic trading
     */
    private void simulateMarketEvent(Stock stock, double priceChange) {
        String[] events = {
                "Breaking: Earnings report released",
                "Market News: Analyst upgrade",
                "Alert: Industry partnership announced",
                "Update: Regulatory approval received",
                "News: Major contract signed"
        };

        String event = events[ThreadLocalRandom.current().nextInt(events.length)];
        String trend = priceChange > 0 ? "🚀 BULLISH" : "🐻 BEARISH";

        System.out.printf("📰 %s for %s - %s movement!\n", event, stock.getSymbol(), trend);
    }

    /**
     * 🛑 Gracefully stop the price updater
     */
    public void stopUpdating() {
        running = false;
        this.interrupt();  // Wake up if sleeping
    }

    /**
     * ⚡ Create market volatility (for testing)
     */
    public void triggerVolatileMarket(int durationSeconds) {
        System.out.println("⚡ VOLATILE MARKET CONDITIONS TRIGGERED!");

        new Thread(() -> {
            try {
                long endTime = System.currentTimeMillis() + (durationSeconds * 1000L);

                while (System.currentTimeMillis() < endTime && running) {
                    // Update prices more frequently during volatility
                    if (stockMarket.isMarketOpen()) {
                        updateRandomStockPrice();
                    }
                    Thread.sleep(2000);  // Update every 2 seconds instead of 10
                }

                System.out.println("⚡ Market volatility ended - Normal trading resumed");
            } catch (InterruptedException e) {
                // Volatility interrupted
            }
        }, "VolatilitySimulator").start();
    }
}