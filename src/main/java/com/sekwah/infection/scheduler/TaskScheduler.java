package com.sekwah.infection.scheduler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Consumer;

/**
 * Tasks will be scheduled based on a number of ticks and will be run on the main thread.
 * <p>
 * Using generics because we may want to run delayed events on players or all sorts.
 * <p>
 * This will help with re-using this code in future projects.
 */
public class TaskScheduler<T> {
    private final ArrayList<DelayedTickEvent<T>> tickEvents = new ArrayList<>();

    public DelayedTickEvent<T> scheduleTickEvent(Consumer<T> consumer, int tickDelay, Consumer<T> consumerOnCancel) {
        var task = new DelayedTickEvent<>(consumer, tickDelay, false, consumerOnCancel);
        tickEvents.add(task);
        return task;
    }

    public DelayedTickEvent<T> scheduleTickEvent(Consumer<T> consumer, int tickDelay) {
            var task = new DelayedTickEvent<>(consumer, tickDelay);
            tickEvents.add(task);
            return task;
    }


    public DelayedTickEvent<T> scheduleIntervalTickEvent(Consumer<T> consumer, int tickDelay, Consumer<T> consumerOnCancel) {
        var task = new DelayedTickEvent<>(consumer, tickDelay, true, consumerOnCancel);
        tickEvents.add(task);
        return task;
    }

    public DelayedTickEvent<T> scheduleIntervalTickEvent(Consumer<T> consumer, int tickDelay) {
        var task = new DelayedTickEvent<>(consumer, tickDelay, true);
        tickEvents.add(task);
        return task;
    }

    public void clearTask(DelayedTickEvent<T> task, T target) {
        task.cancel(target);
    }

    public void tick(T target) {
        Iterator<DelayedTickEvent<T>> iterator = this.tickEvents.iterator();
        while (iterator.hasNext()) {
            DelayedTickEvent<T> event = iterator.next();
            if(event.shouldRemove()) {
                iterator.remove();
            } else {
                event.tick();
                if (event.shouldRun()) {
                    event.run(target);
                    if (!event.isInterval) {
                        iterator.remove();
                    }
                }
            }
        }
    }

    public void clearTasks(T target) {
        Iterator<DelayedTickEvent<T>> iterator = this.tickEvents.iterator();
        while (iterator.hasNext()) {
            DelayedTickEvent<T> event = iterator.next();
            event.cancel(target);
        }
    }
}
