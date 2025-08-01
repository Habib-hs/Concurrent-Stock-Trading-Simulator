package com.habib.tradesimulator.core;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 📈 Stock - Represents a tradeable stock with thread-safe price updates
 *
 * WHY: Multiple threads (traders + price updater) access same stock
 * MODERN FEATURES: ReadWriteLock for better performance, Records for immutability
 * THREAD SAFETY: ReadWriteLock allows multiple readers, exclusive writer
 */
public class Stock {
    private final String symbol;           // Stock symbol (AAPL, GOOGL, etc.)
    private final String name;             // Company name
    private volatile double currentPrice;   // Current stock price (volatile for visibility)
    private final double initialPrice;     // Starting price (for reference)

    // 🔒 ReadWriteLock: Multiple threads can READ price simultaneously,
    // but only one thread can update price at a time
    private final ReadWriteLock priceLock = new ReentrantReadWriteLock();
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public Stock(String symbol, String name, double initialPrice) {
        this.symbol = Objects.requireNonNull(symbol, "Stock symbol cannot be null");
        this.name = Objects.requireNonNull(name, "Stock name cannot be null");
        this.initialPrice = initialPrice;
        this.currentPrice = initialPrice;

        System.out.printf("📈 Stock created: %s (%s) at $%.2f [%s]\n",
                symbol, name, initialPrice, getCurrentTime());
    }

    /**
     * 💰 Update stock price (called by PriceUpdater thread)
     * WHY WriteLock: Only one thread should update price at a time
     */
    public void updatePrice(double newPrice) {
        if (newPrice <= 0) {
            throw new IllegalArgumentException("Stock price must be positive");
        }

        priceLock.writeLock().lock();  // 🔒 Exclusive write access
        try {
            double oldPrice = this.currentPrice;
            this.currentPrice = newPrice;
            double changePercent = ((newPrice - oldPrice) / oldPrice) * 100;

            System.out.printf("💰 %s: $%.2f → $%.2f (%.2f%%) [%s]\n",
                    symbol, oldPrice, newPrice, changePercent, getCurrentTime());
        } finally {
            priceLock.writeLock().unlock();  // Always unlock in finally
        }
    }

    /**
     * 👁️ Get current price (called by multiple trader threads)
     * WHY ReadLock: Multiple traders can read price simultaneously
     */
    public double getCurrentPrice() {
        priceLock.readLock().lock();  // 🔓 Shared read access
        try {
            return currentPrice;
        } finally {
            priceLock.readLock().unlock();
        }
    }

    // Getters (immutable fields don't need synchronization)
    public String getSymbol() { return symbol; }
    public String getName() { return name; }
    public double getInitialPrice() { return initialPrice; }

    /**
     * 📊 Calculate price change percentage since start
     */
    public double getPriceChangePercent() {
        double current = getCurrentPrice();
        return ((current - initialPrice) / initialPrice) * 100;
    }

    private String getCurrentTime() {
        return LocalDateTime.now().format(timeFormatter);
    }

    @Override
    public String toString() {
        return String.format("%s ($%.2f)", symbol, getCurrentPrice());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Stock stock = (Stock) obj;
        return Objects.equals(symbol, stock.symbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol);
    }
}