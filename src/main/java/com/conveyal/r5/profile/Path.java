package com.conveyal.r5.profile;

import com.conveyal.r5.transit.TransitLayer;
import com.conveyal.r5.transit.TripPattern;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Class used to represent transit paths in Browsochrones and Modeify.
 */
public class Path {

    private static final Logger LOG = LoggerFactory.getLogger(Path.class);

    // FIXME assuming < 4 legs on each path, this parallel array implementation probably doesn't use less memory than a List<Leg>.
    // It does effectively allow you to leave out the boardStopPositions and alightStopPositions, but a subclass could do that too.
    public int[] patterns;
    public int[] boardStops;
    public int[] alightStops;
    public int[] alightTimes;
    public int[] boardTimes;
    public int[] transferTimes;
    public int[] trips;
    public int[] boardStopPositions;
    public int[] alightStopPositions;
    public final int length;

    /**
     * Scan over a raptor state and extract the path leading up to that state.
     */
    public Path(RaptorState state, int stop) {
        // trace the path back from this RaptorState
        int previousPattern;
        int previousTrip;
        TIntList patterns = new TIntArrayList();
        TIntList boardStops = new TIntArrayList();
        TIntList alightStops = new TIntArrayList();
        TIntList times = new TIntArrayList();
        TIntList alightTimes = new TIntArrayList();
        TIntList boardTimes = new TIntArrayList();
        TIntList transferTimes = new TIntArrayList();
        TIntList trips = new TIntArrayList();

        while (state.previous != null) {
            // find the fewest-transfers trip that is still optimal in terms of travel time
            if (state.previous.bestNonTransferTimes[stop] == state.bestNonTransferTimes[stop]) {
                state = state.previous;
                continue;
            }

            if (state.previous.bestNonTransferTimes[stop] < state.bestNonTransferTimes[stop]) {
                LOG.error("Previous round has lower weight at stop {}, this implies a bug!", stop);
            }

            previousPattern = state.previousPatterns[stop];
            previousTrip = state.previousTrips[stop];

            patterns.add(previousPattern);
            trips.add(previousTrip);
            alightStops.add(stop);
            times.add(state.bestTimes[stop]);
            alightTimes.add(state.bestNonTransferTimes[stop]);
            boardTimes.add(state.boardTimes[stop]);
            stop = state.previousStop[stop];
            boardStops.add(stop);

            // go to previous state before handling transfers as transfers are done at the end of a round
            state = state.previous;

            // handle transfers
            if (state.transferStop[stop] != -1) {
                transferTimes.add(state.transferTimes[stop]);
                stop = state.transferStop[stop];
            } else {
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

        this.patterns = patterns.toArray();
        this.boardStops = boardStops.toArray();
        this.alightStops = alightStops.toArray();
        this.alightTimes = alightTimes.toArray();
        this.boardTimes = boardTimes.toArray();
        this.trips = trips.toArray();
        this.transferTimes = transferTimes.toArray();
        this.length = this.patterns.length;

        if (length == 0) {
            throw new IllegalStateException("Transit path computed without a transit segment!");
        }
    }

    /**
     * Scan over a mcraptor state and extract the path leading up to that state.
     */
    public Path(McRaptorSuboptimalPathProfileRouter.McRaptorState s) {
        TIntList patterns = new TIntArrayList();
        TIntList boardStops = new TIntArrayList();
        TIntList alightStops = new TIntArrayList();
        TIntList alightTimes = new TIntArrayList();
        TIntList trips = new TIntArrayList();
        TIntList boardStopPositions = new TIntArrayList();
        TIntList alightStopPositions = new TIntArrayList();

        // trace path from this McRaptorState
        do {
            // skip transfers, they are implied
            if (s.pattern == -1) s = s.back;

            patterns.add(s.pattern);
            alightTimes.add(s.time);
            alightStops.add(s.stop);
            boardStops.add(s.back.stop);
            trips.add(s.trip);
            boardStopPositions.add(s.boardStopPosition);
            alightStopPositions.add(s.alightStopPosition);

            s = s.back;
        } while (s.back != null);

        patterns.reverse();
        boardStops.reverse();
        alightStops.reverse();
        alightTimes.reverse();
        trips.reverse();
        boardStopPositions.reverse();
        alightStopPositions.reverse();

        this.patterns = patterns.toArray();
        this.boardStops = boardStops.toArray();
        this.alightStops = alightStops.toArray();
        this.alightTimes = alightTimes.toArray();
        this.trips = trips.toArray();
        this.boardStopPositions = boardStopPositions.toArray();
        this.alightStopPositions = alightStopPositions.toArray();
        this.length = this.patterns.length;

        if (this.patterns.length == 0) {
            throw new IllegalStateException("Transit path computed without a transit segment!");
        }
    }

    public Path(
            int[] patterns,
            int[] boardStops,
            int[] alightStops,
            int[] alightTimes,
            int[] trips,
            int[] boardStopPositions,
            int[] alightStopPositions,
            int[] boardTimes,
            int[] transferTimes
    ) {
        this.patterns = patterns;
        this.boardStops = boardStops;
        this.alightStops = alightStops;
        this.alightTimes = alightTimes;
        this.trips = trips;
        this.boardStopPositions = boardStopPositions;
        this.alightStopPositions = alightStopPositions;
        this.boardTimes = boardTimes;
        this.transferTimes = transferTimes;
        this.length = patterns.length;

        if (patterns.length == 0) {
            throw new IllegalStateException("Transit path computed without a transit segment!");
        }
    }

    /**
     * @see #equals(Object)
     */
    public int hashCode() {
        return length +
                31 * Arrays.hashCode(boardTimes) +
                317 * Arrays.hashCode(boardStops) +
                3117 * Arrays.hashCode(patterns) +
                31117 * Arrays.hashCode(alightStops);
    }

    /**
     * We need to include boardTimes, patterns, boardStops and alightStops to get unique paths.
     * It might be tempting to skip one of these, but you can not. A bus and a tram may visit
     * the same two stops at the same time - pattern is the only thing that differ. Likewise
     * two stops may be so close that the alight/board time is the same for a given route;
     * hence we must include both boardStops and alightStops. Finally, all board times
     * must be included, to include all diffrent variations of trips of the same pattern.
     */
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof Path)) return false;

        Path p = (Path) o;
        return Arrays.equals(boardTimes, p.boardTimes) &&
                Arrays.equals(boardStops, p.boardStops) &&
                Arrays.equals(patterns, p.patterns) &&
                Arrays.equals(alightStops, p.alightStops)
                ;
    }

    public String toString(int accessTransferTime, int egressTransferTime) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (i != 0) s.append("  >>  ");
            s.append(String.format(
                    "%s - %s %6s %5d > %5d",
                    toTime(boardTimes[i] - accessTransferTime),
                    toTime(alightTimes[i] + egressTransferTime),
                    "#" + patterns[i],
                    boardStops[i],
                    alightStops[i])
            );
        }
        return s.toString();
    }

    /**
     * Gets tripPattern at provided pathIndex
     */
    public TripPattern getPattern(TransitLayer transitLayer, int pathIndex) {
        return transitLayer.tripPatterns.get(this.patterns[pathIndex]);
    }

    public int egressStop() {
        return alightStops[length - 1];
    }

    public int accessStop() {
        return boardStops[0];
    }

    public int travelTime() {
        return alightTimes[length - 1] - boardTimes[0];
    }

    private static String toTime(int time) {
        time /= 60;
        int min = time % 60;
        int hour = time / 60;
        return String.format("%02d:%02d", hour, min);
    }
}
