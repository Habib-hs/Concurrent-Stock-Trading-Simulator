package com.habib.tradesimulator.practise;

public class ThreadLifeCycle {
    public static void main(String[] args) throws InterruptedException {

        // 🧵 Thread 1 - গণনা করবে
        Thread counter = new Thread(() -> {
            for(int i = 1; i <= 3; i++) {
                System.out.println("Count: " + i);
                try {
                    Thread.sleep(1000); // 1 সেকেন্ড বিরতি
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Counter শেষ!");
        });

        // 🧵 Thread 2 - বার্তা প্রিন্ট করবে
        Thread messenger = new Thread(() -> {
            System.out.println("Messenger: হ্যালো!");
            try {
                counter.join(); // Counter শেষ হওয়ার জন্য অপেক্ষা করবে
                System.out.println("Messenger: Counter শেষ হয়ে গেছে!");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        // State দেখি
        System.out.println("Counter state: " + counter.getState()); // NEW
        System.out.println("Messenger state: " + messenger.getState()); // NEW

        // দুইটা thread চালু করি
        counter.start();
        messenger.start();

        // একটু wait করে state দেখি
        Thread.sleep(1500);
        System.out.println("Counter state (মাঝে): " + counter.getState()); // TIMED_WAITING
        System.out.println("Messenger state (মাঝে): " + messenger.getState()); // WAITING

        // সব শেষ হওয়ার জন্য অপেক্ষা
        counter.join();
        messenger.join();

        System.out.println("Final - Counter state: " + counter.getState()); // TERMINATED
        System.out.println("Final - Messenger state: " + messenger.getState()); // TERMINATED
    }
}