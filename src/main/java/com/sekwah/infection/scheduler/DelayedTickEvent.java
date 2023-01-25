package com.sekwah.infection.scheduler;

import java.util.function.Consumer;

public class DelayedTickEvent<T> {

    /**
     * The only reason this is not final is that we may want to keep running it until it gets to the last stage.
     */
    public boolean isInterval;

    private boolean shouldRemove = false;
    private final int maxTicks;
    private final Consumer<T> consumer;

    /**
     * Code to run if the event is cancelled. E.g. resetting something.
     */
    private final Consumer<T> cancelConsumer;
    private int ticks;

    public DelayedTickEvent(Consumer<T> consumer, int ticks, boolean isInterval, Consumer<T> cancelConsumer) {
        this.consumer = consumer;
        this.ticks = ticks;
        this.maxTicks = ticks;
        this.isInterval = isInterval;
        this.cancelConsumer = cancelConsumer;
    }

    public DelayedTickEvent(Consumer<T> consumer, int ticks, boolean isInterval) {
        this(consumer, ticks, isInterval, (test) -> {});
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

    public boolean shouldRemove() {
        return this.shouldRemove;
    }

    public void cancel(T target) {
        if(this.shouldRemove) {
            return;
        }
        this.shouldRemove = true;
        this.cancelConsumer.accept(target);
    }

    public void run(T target) {
        this.consumer.accept(target);
        if(this.isInterval) {
            this.ticks = this.maxTicks;
        }
    }

    public void scheduleTick() {
        this.ticks = 0;
    }

    public int getTicksLeft() {
        return this.ticks;
    }
}