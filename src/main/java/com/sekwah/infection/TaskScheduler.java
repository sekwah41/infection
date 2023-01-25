package com.sekwah.infection;

import com.sekwah.infection.scheduler.DelayedTickEvent;

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
    private ArrayList<DelayedTickEvent<T>> tickEvents = new ArrayList<>();

    public void scheduleTickEvent(Consumer<T> consumer, int tickDelay) {
        tickEvents.add(new DelayedTickEvent<>(consumer, tickDelay));
    }

    public void scheduleIntervalTickEvent(Consumer<T> consumer, int tickDelay) {
        tickEvents.add(new DelayedTickEvent<>(consumer, tickDelay, true));
    }

    public void tick(T target) {
        Iterator<DelayedTickEvent<T>> iterator = this.tickEvents.iterator();
        while (iterator.hasNext()) {
            DelayedTickEvent<T> event = iterator.next();
            event.tick();
            if (event.shouldRun()) {
                event.run(target);
                if (!event.isInterval) {
                    iterator.remove();
                }
            }
        }
    }

    public void clearTasks(T target) {
        for( DelayedTickEvent<T> event : this.tickEvents) {
            event.cancel(target);
        }
        tickEvents.clear();
    }
}
