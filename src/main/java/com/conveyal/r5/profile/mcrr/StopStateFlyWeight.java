package com.conveyal.r5.profile.mcrr;


import static com.conveyal.r5.profile.mcrr.IntUtils.newIntArray;
import static com.conveyal.r5.util.TimeUtils.timeToString;

public final class StopStateFlyWeight implements StopState {

    private int size = 0;
    private int cursor = NOT_SET;

    private final int[][] stateStopIndex;


    private final int[] times;
    private final int[] transitTimes;
    private final int[] previousPatterns;
    private final int[] previousTrips;
    private final int[] boardTimes;
    private final int[] transferTimes;
    private final int[] boardStops;
    private final int[] transferFromStops;


    StopStateFlyWeight(int rounds, int stops) {
        this.stateStopIndex = new int[rounds][stops];

        final int limit = 3 * stops;

        this.times = newIntArray(limit, UNREACHED);

        this.boardStops = newIntArray(limit, NOT_SET);
        this.transitTimes = newIntArray(limit, UNREACHED);
        this.previousPatterns = newIntArray(limit, NOT_SET);
        this.previousTrips = newIntArray(limit, NOT_SET);
        this.boardTimes = newIntArray(limit, UNREACHED);

        this.transferFromStops = newIntArray(limit, NOT_SET);
        this.transferTimes = newIntArray(limit, NOT_SET);
    }

    public void transitToStop(int round, int stop, int time, int fromPattern, int boardStop, int tripIndex, int boardTime, boolean bestTime) {
        final int index = findOrCreateStopIndex(round, stop);

        transitTimes[index] = time;
        previousPatterns[index] = fromPattern;
        previousTrips[index] = tripIndex;
        boardTimes[index] = boardTime;
        boardStops[index] = boardStop;

        if(bestTime) {
            times[index] = time;
            transferFromStops[index] = NOT_SET;
        }
    }

    /**
     * Set the time at a transit index iff it is optimal. This sets both the best time and the nonTransferTime
     */
    public void transferToStop(int round, int stop, int time, int fromStop, int transferTime) {
        final int index = findOrCreateStopIndex(round, stop);
        times[index] = time;
        transferFromStops[index] = fromStop;
        transferTimes[index] = transferTime;
    }

    public void setInitalTime(int round, int stop, int time) {
        final int index = findOrCreateStopIndex(round, stop);
        times[index] = time;
    }

    public final int time(int round, int stop) {
        final int index = stateStopIndex[round][stop];
        return times[index];
    }

    public void setCursor(int round, int stop) {
        this.cursor = stateStopIndex[round][stop];
    }

    @Override
    public final int time() {
        return times[cursor];
    }
    @Override
    public int transitTime() {
        return transitTimes[cursor];
    }

    @Override
    public boolean isTransitTimeSet() {
        return transitTimes[cursor] != UNREACHED;
    }

    @Override
    public int previousPattern() {
        return previousPatterns[cursor];
    }

    @Override
    public int previousTrip() {
        return previousTrips[cursor];
    }

    @Override
    public int transferTime() {
        return transferTimes[cursor];
    }

    @Override
    public int boardStop() {
        return boardStops[cursor];
    }

    @Override
    public int boardTime() {
        return boardTimes[cursor];
    }

    @Override
    public int transferFromStop() {
        return transferFromStops[cursor];
    }

    @Override
    public boolean arrivedByTransfer() {
        return transferFromStops[cursor] != NOT_SET;
    }

    public int nextAvailable() {
        // Skip the first element, index 0 is not used for optimaziations reasons
        return ++size;
    }


    static final String[] HEADERS = {
            "- TRANSFER FROM -   ----------- TRANSIT -----------",
            " Time   Stop  Dur    Time B.Stop B.Time  Pttrn Trip"
    };

    public String stopToString(int round, int stop) {
        final int stopIndex = stateStopIndex[round][stop];
        return String.format("%5s %6s %4s   %5s %6s %6s %6s %4s",
                timeToString(times[stopIndex], UNREACHED),
                intToString(transferFromStops[stopIndex]),
                intToString(transferTimes[stopIndex]),
                timeToString(transitTimes[stopIndex], UNREACHED),
                intToString(boardStops[stopIndex]),
                timeToString(boardTimes[stopIndex], UNREACHED),
                intToString(previousPatterns[stopIndex]),
                intToString(previousTrips[stopIndex])
        );
    }

    private static String intToString(int value) { return value == -1 ? "" : Integer.toString(value); }

    private int findOrCreateStopIndex(final int round, final int stop) {
        if(stateStopIndex[round][stop] == 0) {
            stateStopIndex[round][stop] = nextAvailable();
        }
        return stateStopIndex[round][stop];
    }

}
