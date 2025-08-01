package com.habib.tradesimulator.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 💼 Portfolio - Thread-safe trader's wallet and stock holdings
 *
 * WHY: Each trader has one portfolio, but trader thread does complex operations
 * MODERN FEATURES: ConcurrentHashMap, AtomicReference, Stream API
 * THREAD SAFETY: Synchronized methods for atomic operations
 */
public class Portfolio {
    private final String ownerName;
    private final AtomicReference<Double> cashBalance;  // Atomic for thread-safe updates

    // ConcurrentHashMap: Thread-safe map for stock holdings
    private final Map<String, Integer> holdings = new ConcurrentHashMap<>();

    public Portfolio(String ownerName, double initialCash) {
        this.ownerName = ownerName;
        this.cashBalance = new AtomicReference<>(initialCash);

        System.out.printf("💼 Portfolio created for %s with $%.2f\n", ownerName, initialCash);
    }

    /**
     * 🛒 Attempt to buy stocks
     * WHY synchronized: Multiple operations must be atomic (check cash + deduct + add stocks)
     */
    public synchronized boolean buyStock(String stockSymbol, int quantity, double pricePerShare) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        double totalCost = quantity * pricePerShare;
        double currentCash = cashBalance.get();

        if (currentCash >= totalCost) {
            // Update cash atomically
            cashBalance.set(currentCash - totalCost);

            // Update holdings (ConcurrentHashMap handles thread safety)
            holdings.merge(stockSymbol, quantity, Integer::sum);  // Modern way to add/update

            System.out.printf("✅ %s BOUGHT %d %s @ $%.2f (Total: $%.2f, Remaining: $%.2f)\n",
                    ownerName, quantity, stockSymbol, pricePerShare, totalCost, cashBalance.get());
            return true;
        } else {
            System.out.printf("❌ %s FAILED BUY %s - Insufficient funds (Need: $%.2f, Have: $%.2f)\n",
                    ownerName, stockSymbol, totalCost, currentCash);
            return false;
        }
    }

    /**
     * 💰 Attempt to sell stocks
     * WHY synchronized: Check holdings + remove stocks + add cash must be atomic
     */
    public synchronized boolean sellStock(String stockSymbol, int quantity, double pricePerShare) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        int currentHolding = holdings.getOrDefault(stockSymbol, 0);

        if (currentHolding >= quantity) {
            double totalRevenue = quantity * pricePerShare;

            // Update cash atomically
            cashBalance.updateAndGet(current -> current + totalRevenue);

            // Update holdings
            if (currentHolding == quantity) {
                holdings.remove(stockSymbol);  // Sold all shares
            } else {
                holdings.put(stockSymbol, currentHolding - quantity);
            }

            System.out.printf("💰 %s SOLD %d %s @ $%.2f (Total: $%.2f, Cash: $%.2f)\n",
                    ownerName, quantity, stockSymbol, pricePerShare, totalRevenue, cashBalance.get());
            return true;
        } else {
            System.out.printf("❌ %s FAILED SELL %s - Insufficient shares (Need: %d, Have: %d)\n",
                    ownerName, stockSymbol, quantity, currentHolding);
            return false;
        }
    }

    // Thread-safe getters
    public double getCashBalance() {
        return cashBalance.get();
    }

    public int getStockQuantity(String stockSymbol) {
        return holdings.getOrDefault(stockSymbol, 0);
    }

    /**
     * 📊 Calculate total portfolio value (cash + stock values)
     */
    public synchronized double calculateTotalValue(Map<String, Double> stockPrices) {
        double stockValue = holdings.entrySet().stream()
                .mapToDouble(entry -> {
                    String symbol = entry.getKey();
                    int quantity = entry.getValue();
                    double price = stockPrices.getOrDefault(symbol, 0.0);
                    return quantity * price;
                })
                .sum();

        return cashBalance.get() + stockValue;
    }

    public synchronized void printPortfolio() {
        System.out.println("\n📊 " + ownerName + "'s Portfolio:");
        System.out.printf("💵 Cash: $%.2f\n", cashBalance.get());
        System.out.println("📈 Holdings:");

        if (holdings.isEmpty()) {
            System.out.println("   (No stocks owned)");
        } else {
            // Using Stream API for modern formatting
            String holdingsStr = holdings.entrySet().stream()
                    .map(entry -> String.format("   %s: %d shares", entry.getKey(), entry.getValue()))
                    .collect(Collectors.joining("\n"));
            System.out.println(holdingsStr);
        }
        System.out.println();
    }

    public String getOwnerName() {
        return ownerName;
    }
}