package com.habib.tradesimulator;

import com.habib.tradesimulator.core.*;
import com.habib.tradesimulator.market.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * 🏛️ Main - Stock Trading Floor Simulator
 *
 * REAL WORLD ANALOGY: You are the STOCK EXCHANGE MANAGER
 * - You open the trading floor (market)
 * - You register stocks for trading
 * - You allow 5 human traders to start trading
 * - You monitor the market and all activities
 * - You close the market and generate reports
 */
public class Main {

    // 🏛️ Market Infrastructure
    private static StockMarket stockMarket;
    private static PriceUpdater priceUpdater;
    private static TradeCounter tradeCounter;

    // 👥 Trading Participants
    private static List<Trader> traders;
    private static List<Thread> traderThreads;

    public static void main(String[] args) {
        System.out.println("🏛️ Welcome to the Concurrent Stock Trading Simulator!");
        System.out.println("=" .repeat(60));

        try {
            // Step 1: Initialize Market Infrastructure
            setupMarketInfrastructure();

            // Step 2: Register Stocks for Trading
            registerStocksInMarket();

            // Step 3: Create Human Traders
            createTraders();

            // Step 4: Open Market and Start Trading
            startTradingSession();

            // Step 5: Run Interactive Market Monitoring
            runMarketMonitoring();

            // Step 6: Close Market and Generate Reports
            closeMarketAndGenerateReports();

        } catch (Exception e) {
            System.err.println("❌ Trading session error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 🏗️ Step 1: Setup Market Infrastructure
     * REAL WORLD: Setting up the stock exchange building, computers, networks
     */
    private static void setupMarketInfrastructure() {
        System.out.println("\n🏗️ Setting up market infrastructure...");

        // Create the stock exchange
        stockMarket = new StockMarket();

        // Create trade monitoring system
        tradeCounter = new TradeCounter();

        // Create price update system (simulates market forces)
        priceUpdater = new PriceUpdater(stockMarket);

        System.out.println("✅ Market infrastructure ready!");
    }

    /**
     * 📈 Step 2: Register Stocks in Market
     * REAL WORLD: Companies like Apple, Google, Microsoft get listed on exchange
     */
    private static void registerStocksInMarket() {
        System.out.println("\n📈 Registering stocks in market...");

        // Register major technology stocks
        stockMarket.registerStock(new Stock("AAPL", "Apple Inc.", 150.00));
        stockMarket.registerStock(new Stock("GOOGL", "Alphabet Inc.", 2500.00));
        stockMarket.registerStock(new Stock("MSFT", "Microsoft Corporation", 300.00));

        System.out.println("✅ All stocks registered and ready for trading!");
    }

    /**
     * 👥 Step 3: Create Human Traders
     * REAL WORLD: 5 professional traders arrive at the trading floor with their capital
     */
    private static void createTraders() {
        System.out.println("\n👥 Creating traders...");

        traders = new ArrayList<>();
        traderThreads = new ArrayList<>();

        // Create 5 traders with different capital amounts
        String[] traderNames = {"Alice", "Bob", "Charlie", "Diana", "Eve"};
        double[] initialCapital = {10000.0, 15000.0, 8000.0, 20000.0, 12000.0};

        for (int i = 0; i < traderNames.length; i++) {
            Trader trader = new Trader(traderNames[i], initialCapital[i], stockMarket, tradeCounter);
            traders.add(trader);

            // Each trader gets their own thread (like having their own trading desk)
            Thread traderThread = new Thread(trader, "Trader-" + traderNames[i]);
            traderThreads.add(traderThread);
        }

        System.out.printf("✅ %d traders created and ready!\n", traders.size());
    }

    /**
     * 🔔 Step 4: Open Market and Start Trading
     * REAL WORLD: Ring the opening bell - trading begins!
     */
    private static void startTradingSession() {
        System.out.println("\n🔔 OPENING BELL - MARKET IS NOW OPEN!");
        System.out.println("🚀 Starting trading session...");

        // Open the market
        stockMarket.openMarket();

        // Start price update system (market forces)
        priceUpdater.start();

        // Start all trader threads (traders begin trading)
        for (Thread traderThread : traderThreads) {
            traderThread.start();
        }

        System.out.println("✅ All systems operational - Trading in progress!");
    }

    /**
     * 📊 Step 5: Interactive Market Monitoring
     * REAL WORLD: Market supervisor monitoring trading floor activity
     */
    private static void runMarketMonitoring() throws InterruptedException, IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.println("\n📊 MARKET MONITORING DASHBOARD");
        System.out.println("Commands: [status] [summary] [movers] [volatility] [stop]");

        while (stockMarket.isMarketOpen()) {
            System.out.print("\n💻 Enter command (or wait 30s for auto-stop): ");

            // Wait for user input with timeout
            long startTime = System.currentTimeMillis();
            boolean hasInput = false;

            while (System.currentTimeMillis() - startTime < 30000) { // 30 second timeout
                if (System.in.available() > 0) {
                    hasInput = true;
                    break;
                }
                Thread.sleep(100);
            }

            String command;
            if (hasInput) {
                command = scanner.nextLine().trim().toLowerCase();
            } else {
                command = "stop"; // Auto-stop after 30 seconds
                System.out.println("stop (auto-timeout)");
            }

            switch (command) {
                case "status" -> displayTradingStatus();
                case "summary" -> stockMarket.printMarketSummary();
                case "movers" -> stockMarket.printTopMovers();
                case "volatility" -> triggerMarketVolatility();
                case "stop" -> {
                    System.out.println("🛑 Initiating market closure...");
                    return;
                }
                default -> System.out.println("❓ Unknown command. Try: status, summary, movers, volatility, stop");
            }
        }
    }

    /**
     * 📊 Display real-time trading status
     */
    private static void displayTradingStatus() {
        System.out.println("\n📊 REAL-TIME TRADING STATUS:");
        System.out.println("-".repeat(40));

        tradeCounter.printSummary();

        System.out.println("👥 Active Traders:");
        traders.forEach(trader -> {
            System.out.printf("   %s: $%.2f cash\n",
                    trader.getName(), trader.getPortfolio().getCashBalance());
        });
    }

    /**
     * ⚡ Trigger market volatility for excitement
     */
    private static void triggerMarketVolatility() {
        System.out.println("⚡ Triggering market volatility for 15 seconds...");
        priceUpdater.triggerVolatileMarket(15);
    }

    /**
     * 🔔 Step 6: Close Market and Generate Final Reports
     * REAL WORLD: Closing bell - end of trading day, calculate P&L
     */
    private static void closeMarketAndGenerateReports() throws InterruptedException {
        System.out.println("\n🔔 CLOSING BELL - MARKET IS NOW CLOSED!");

        // Close market (stops new trades)
        stockMarket.closeMarket();

        // Stop price updater
        priceUpdater.stopUpdating();

        // Stop all traders
        traders.forEach(Trader::stopTrading);

        // Wait for all traders to finish their last trades
        System.out.println("⏳ Waiting for traders to complete final trades...");
        for (Thread traderThread : traderThreads) {
            traderThread.join(5000); // Wait max 5 seconds per trader
        }

        // Wait for price updater to stop
        priceUpdater.join(2000);

        // Generate comprehensive reports
        generateFinalReports();
    }

    /**
     * 📋 Generate comprehensive end-of-day reports
     */
    private static void generateFinalReports() {
        System.out.println("\n📋 GENERATING END-OF-DAY REPORTS...");
        System.out.println("=".repeat(60));

        // Market Summary
        stockMarket.printMarketSummary();
        stockMarket.printTopMovers();

        // Trading Statistics
        tradeCounter.printSummary();

        // Individual Trader Performance
        System.out.println("\n👥 INDIVIDUAL TRADER PERFORMANCE:");
        System.out.println("=".repeat(50));

        double totalPortfolioValue = 0;
        for (Trader trader : traders) {
            trader.printFinalPortfolio();
            double value = trader.getPortfolio().calculateTotalValue(stockMarket.getCurrentPrices());
            totalPortfolioValue += value;
        }

        // Market Statistics
        System.out.println("\n📈 MARKET STATISTICS:");
        System.out.println("=".repeat(30));
        System.out.printf("💰 Total Market Value: $%.2f\n", totalPortfolioValue);
        System.out.printf("👥 Total Participants: %d traders\n", traders.size());
        System.out.printf("📊 Total Stocks: %d symbols\n", stockMarket.getAllStocks().size());

        System.out.println("\n🎉 Trading session completed successfully!");
        System.out.println("Thank you for using the Concurrent Stock Trading Simulator!");
    }
}