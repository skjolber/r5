package com.conveyal.r5.profile.entur.rangeraptor.view;

import java.util.Collection;

/**
 * TODO TGR
 *
 * @param <T> The TripSchedule type defined by the user of the range raptor API.
 */
public interface DebugHandler<T> {
    void setIterationDepartureTime(int departureTime);
    boolean isDebug(int stop);
    void accept(T element, Collection<? extends T> result);
    void reject(T element, Collection<? extends T> result);
    void drop(T element, T droppedByElement);

    static <S> DebugHandler<S> noop() {
        return new DebugHandler<S>() {
            @Override public void setIterationDepartureTime(int depatureTime) {}
            @Override public boolean isDebug(int stop) { return false; }
            @Override public void accept(S element, Collection<? extends S> result) {}
            @Override public void reject(S element, Collection<? extends S> result) {}
            @Override public void drop(S element, S droppedByElement) {}
        };
    }
}
