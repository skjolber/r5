package com.conveyal.r5.profile.entur;

import com.conveyal.r5.profile.entur.api.TuningParameters;
import com.conveyal.r5.profile.entur.api.path.Path;
import com.conveyal.r5.profile.entur.api.request.RangeRaptorRequest;
import com.conveyal.r5.profile.entur.api.transit.TransitDataProvider;
import com.conveyal.r5.profile.entur.api.transit.TripScheduleInfo;
import com.conveyal.r5.profile.entur.rangeraptor.Worker;
import com.conveyal.r5.profile.entur.rangeraptor.debug.WorkerPerformanceTimers;
import com.conveyal.r5.profile.entur.rangeraptor.multicriteria.McRangeRaptorWorker;
import com.conveyal.r5.profile.entur.rangeraptor.standard.RangeRaptorWorker;

import java.util.Collection;

/**
 * A service for performing Range Raptor routing request.
 *
 * @param <T> The TripSchedule type defined by the user of the range raptor API.
 */
public class RangeRaptorService<T extends TripScheduleInfo> {
    private static final WorkerPerformanceTimers MC_TIMERS = new WorkerPerformanceTimers("MC");
    private static final WorkerPerformanceTimers RR_TIMERS = new WorkerPerformanceTimers("RR");

    private final TuningParameters tuningParameters;

    public RangeRaptorService(TuningParameters tuningParameters) {
        this.tuningParameters = tuningParameters;
    }

    public Collection<Path<T>> route(RangeRaptorRequest<T> request, TransitDataProvider<T> transitData) {
        Worker<T> worker = createWorker(request, transitData);
        return worker.route();
    }


    /* private methods */

    private Worker<T> createWorker(RangeRaptorRequest<T> request, TransitDataProvider<T> transitData) {
        switch (request.profile) {
            case MULTI_CRITERIA_RANGE_RAPTOR:
                return createMcRRWorker(transitData, request);
            case RAPTOR_REVERSE:
            case RANGE_RAPTOR:
                return createRRWorker(transitData, request);
            default:
                throw new IllegalStateException("Unknown profile: " + this);
        }
    }

    private Worker<T> createMcRRWorker(TransitDataProvider<T> transitData, RangeRaptorRequest<T> request) {
        return new McRangeRaptorWorker<>(tuningParameters, transitData, request, MC_TIMERS);
    }

    private Worker<T> createRRWorker(TransitDataProvider<T> transitData, RangeRaptorRequest<T> request) {
        return new RangeRaptorWorker<>(tuningParameters, transitData, request, RR_TIMERS);
    }

}
