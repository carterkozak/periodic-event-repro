package net.ckozak.repro;

import jdk.jfr.Configuration;
import jdk.jfr.Recording;
import jdk.jfr.consumer.EventStream;

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
        try (Recording recording = new Recording()) {
            recording.setToDisk(true);
            recording.setDumpOnExit(true);
            recording.setName("my-recording");
            recording.setSettings(Configuration.getConfiguration("default").getSettings());
            recording.start();
            AtomicInteger events = new AtomicInteger();
            try (EventStream eventStream = EventStream.openRepository()) {
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
    }

    private Main() {
    }
}
