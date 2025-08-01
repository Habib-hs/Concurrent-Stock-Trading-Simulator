package com.habib.tradesimulator.core;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

/**
 * 📊 TradeCounter - Shared statistics across all traders
 *
 * WHY: All traders contribute to same counter (SHARED RESOURCE = RACE CONDITION RISK!)
 * MODERN FEATURES: AtomicInteger, LongAdder for high-performance counting
 * THREAD SAFETY: Shows both synchronized and atomic approaches
 */
public class TradeCounter {

    // 🔥 RACE CONDITION VERSION (will show incorrect results)
    private int unsafeTotalTrades = 0;

    // ✅ THREAD-SAFE VERSIONS
    private final AtomicInteger safeTotalTrades = new AtomicInteger(0);
    private final LongAdder successfulTrades = new LongAdder();  // High-performance counter
    private final LongAdder failedTrades = new LongAdder();

    // Synchronized version for comparison
    private int syncTotalTrades = 0;

    /**
     * ❌ UNSAFE INCREMENT - Will cause race conditions!
     * Demo purpose: Shows what happens without synchronization
     */
    public void unsafeIncrement() {
        unsafeTotalTrades++;  // NOT THREAD-SAFE: read + increment + write
    }

    /**
     * 🔒 SYNCHRONIZED INCREMENT - Thread-safe but slower
     */
    public synchronized void syncIncrement() {
        syncTotalTrades++;
    }

    /**
     * ⚡ ATOMIC INCREMENT - Thread-safe and fast
     * Modern approach: AtomicInteger uses lock-free algorithms
     */
    public void atomicIncrement() {
        safeTotalTrades.incrementAndGet();
    }

    /**
     * 🎯 Record successful trade (modern way)
     */
    public void recordSuccessfulTrade() {
        successfulTrades.increment();  // LongAdder: even faster for high contention
        atomicIncrement();

        // Print milestone every 10 trades
        if (safeTotalTrades.get() % 10 == 0) {
            System.out.printf("📈 Milestone: %d total trades completed!\n", safeTotalTrades.get());
        }
    }

    /**
     * 💥 Record failed trade
     */
    public void recordFailedTrade() {
        failedTrades.increment();
        atomicIncrement();
    }

    // Getters for different counter versions
    public int getUnsafeTotalTrades() { return unsafeTotalTrades; }
    public int getSyncTotalTrades() { return syncTotalTrades; }
    public int getSafeTotalTrades() { return safeTotalTrades.get(); }
    public long getSuccessfulTrades() { return successfulTrades.sum(); }
    public long getFailedTrades() { return failedTrades.sum(); }

    /**
     * 📋 Print comprehensive trading statistics
     */
    public void printSummary() {
        System.out.println("\n🏆 TRADING STATISTICS SUMMARY:");
        System.out.println("=" .repeat(50));

        // Show race condition demonstration
        System.out.printf("❌ Unsafe Counter: %d (Race conditions!)\n", unsafeTotalTrades);
        System.out.printf("🔒 Sync Counter: %d (Thread-safe, slower)\n", syncTotalTrades);
        System.out.printf("⚡ Atomic Counter: %d (Thread-safe, faster)\n", safeTotalTrades.get());

        System.out.println("-".repeat(30));
        System.out.printf("✅ Successful Trades: %d\n", successfulTrades.sum());
        System.out.printf("❌ Failed Trades: %d\n", failedTrades.sum());

        long total = successfulTrades.sum() + failedTrades.sum();
        if (total > 0) {
            double successRate = (double) successfulTrades.sum() / total * 100;
            System.out.printf("📈 Success Rate: %.1f%%\n", successRate);
        }

        System.out.println("=" .repeat(50));
    }

    /**
     * 🧪 Demonstrate race condition with multiple threads
     */
    public void demonstrateRaceCondition(int iterations) throws InterruptedException {
        System.out.println("🧪 Demonstrating Race Condition...");

        // Reset counters
        unsafeTotalTrades = 0;
        syncTotalTrades = 0;
        safeTotalTrades.set(0);

        // Create multiple threads that increment counters
        Thread[] threads = new Thread[5];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < iterations; j++) {
                    unsafeIncrement();  // Race condition
                    syncIncrement();    // Thread-safe
                    atomicIncrement();  // Thread-safe
                }
            });
        }

        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }

        // Wait for completion
        for (Thread thread : threads) {
            thread.join();
        }

        int expected = threads.length * iterations;
        System.out.printf("Expected: %d, Unsafe: %d, Sync: %d, Atomic: %d\n",
                expected, unsafeTotalTrades, syncTotalTrades, safeTotalTrades.get());
    }
}