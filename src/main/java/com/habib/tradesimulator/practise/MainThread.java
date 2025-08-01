package com.habib.tradesimulator.practise;

public class MainThread {
    public static void main(String[] args) {
        System.out.println(" Main thread শুরু হলো!");

        // 🧵 নতুন thread তৈরি করি
        Thread workerThread = new Thread(() -> {
            for(int i = 1; i <= 5; i++) {
                System.out.println("  Worker thread: " + i);
                try {
                    Thread.sleep(800); // 0.8 সেকেন্ড wait
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("  ✅ Worker thread শেষ!");
        });

        // Thread চালু করি
        workerThread.start();

        // Main thread এও কাজ করি
        for(int i = 1; i <= 3; i++) {
            System.out.println("Main thread: " + i);
            try {
                Thread.sleep(1000); // 1 সেকেন্ড wait
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("✅ Main thread শেষ!");
    }
}