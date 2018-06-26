package com.conveyal.r5.profile.mcrr;

import com.conveyal.r5.profile.Path;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;


/**
 * Class used to represent transit paths in Browsochrones and Modeify.
 */
public class McPathBuilder {
    private TIntList patterns = new TIntArrayList();
    private TIntList boardStops = new TIntArrayList();
    private TIntList alightStops = new TIntArrayList();
//    private TIntList times = new TIntArrayList();
    private TIntList alightTimes = new TIntArrayList();
    private TIntList boardTimes = new TIntArrayList();
    private TIntList transferTimes = new TIntArrayList();
    private TIntList trips = new TIntArrayList();


    /**
     * Scan over a raptor state and extract the path leading up to that state.
     */
    public  Path extractPathForStop(McRaptorState state, int stop) {
        if(!state.isStopReachedByTransit(stop)) {
            return null;
        }
        // trace the path back from this RaptorState
        patterns.clear();
        boardStops.clear();
        alightStops.clear();
//        times.clear();
        alightTimes.clear();
        boardTimes.clear();
        transferTimes.clear();
        trips.clear();

        McRaptorState.debugStopHeader("FIND PATH");

        // find the fewest-transfers trip that is still optimal in terms of travel time
        state.findLastRoundWithTransitTimeSet(stop);

        if(state.round() == 0) {
            return null;
        }


        state.debugStop("egress stop", state.round(), stop);

        while (state.round() > 0) {
            StopState it = state.stop(stop);

            patterns.add(it.previousPattern());
            trips.add(it.previousTrip());
            alightStops.add(stop);
//            times.add(it.time());
            boardTimes.add(it.boardTime());
            alightTimes.add(it.transitTime());

            // TODO
            stop = it.boardStop();

            boardStops.add(stop);

            // go to previous state before handling transfers as transfers are done at the end of a round
            state.gotoPreviousRound();
            it = state.stop(stop);
            state.debugStop("by", state.round(), stop);


            // handle transfers
            if (it.arrivedByTransfer()) {
                transferTimes.add(it.time());
                stop = it.transferFromStop();
                state.debugStop("transfer", state.round(), stop);
            }
            else {
                transferTimes.add(-1);
            }
        }

        // we traversed up the tree but the user wants to see paths down the tree
        // TODO when we do reverse searches we won't want to reverse paths
        patterns.reverse();
        boardStops.reverse();
        alightStops.reverse();
        alightTimes.reverse();
        boardTimes.reverse();
        trips.reverse();
        transferTimes.reverse();

        return new Path(
                patterns.toArray(),
                boardStops.toArray(),
                alightStops.toArray(),
                alightTimes.toArray(),
                trips.toArray(),
                null,
                null,
                boardTimes.toArray(),
                transferTimes.toArray()
        );
    }
}
