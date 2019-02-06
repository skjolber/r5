package com.conveyal.r5.profile.entur.api;


/**
 * Tuning parameters - changing these parameters change the performance (speed and/or memory consumption).
 */
public interface TuningParameters {

    /**
     * This parameter is used to allocate enough memory space for Raptor.
     * Set it to the maximum number of transfers for any given itinerary expected to
     * be found within the entire transit network.
     * <p/>
     * Default value is 12.
     */
    default int maxNumberOfTransfers() {
        return 12;
    }


    /**
     * This threshold is used to determine when to perform a binary trip schedule search
     * to reduce the number of trips departure time lookups and comparisons. When testing
     * with data from Entur and all of Norway as a Graph, the optimal value was about 50.
     * <p/>
     * If you calculate the departure time every time or want to fine tune the performance,
     * changing this may improve the performance a few percent.
     */
    default int scheduledTripBinarySearchThreshold() {
        return 50;
    }
}
