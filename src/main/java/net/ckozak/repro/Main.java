package net.ckozak.repro;

import jdk.jfr.Configuration;
import jdk.jfr.Recording;
import jdk.jfr.consumer.EventStream;
import jdk.jfr.consumer.RecordingStream;

import java.util.concurrent.atomic.AtomicInteger;

public final class Main {

    public static void main(String[] _args) throws Exception {
        for (int i = 0; i < 10; i++) {
            System.out.printf("Iteration %d%n", i);
            operation();
            Thread.sleep(1_000);
        }
    }

    public static void operation() throws Exception {
        AtomicInteger events = new AtomicInteger();
        try (RecordingStream eventStream = new RecordingStream()) {
            eventStream.enable("jdk.JavaThreadStatistics").with("period", "1000 ms");
            eventStream.onEvent("jdk.JavaThreadStatistics", event -> {
                System.out.printf("Event: %s%n", event);
                events.incrementAndGet();
            });
            eventStream.startAsync();
            long startTime = System.nanoTime();
            while (events.get() == 0 && System.nanoTime() - startTime < 10_000_000_000L) {
                Thread.sleep(100);
            }
        }
        if (events.get() == 0) {
            throw new RuntimeException("No events detected");
        }
    }

    private Main() {
    }
}
