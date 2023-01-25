package com.sekwah.infection.scheduler;

import java.util.function.Consumer;

public class DelayedTickEvent<T> {

    /**
     * The only reason this is not final is that we may want to keep running it until it gets to the last stage.
     */
    public boolean isInterval;
    private final int maxTicks;
    public Consumer<T> consumer;
    public int ticks;

    public DelayedTickEvent(Consumer<T> consumer, int ticks, boolean isInterval) {
        this.consumer = consumer;
        this.ticks = ticks;
        this.maxTicks = ticks;
        this.isInterval = isInterval;
    }

    public DelayedTickEvent(Consumer<T> consumer, int ticks) {
        this(consumer, ticks, false);
    }

    public void tick() {
        this.ticks--;
    }

    public boolean shouldRun() {
        return this.ticks <= 0;
    }

    public void run(T target) {
        this.consumer.accept(target);
        if(this.isInterval) {
            this.ticks = this.maxTicks;
        }
    }
}