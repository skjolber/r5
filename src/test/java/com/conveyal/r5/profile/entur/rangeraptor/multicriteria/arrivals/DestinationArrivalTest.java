package com.conveyal.r5.profile.entur.rangeraptor.multicriteria.arrivals;

import com.conveyal.r5.profile.entur.api.transit.EgressLeg;
import com.conveyal.r5.profile.entur.api.transit.TripScheduleInfo;
import com.conveyal.r5.profile.entur.rangeraptor.transit.TransitCalculator;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DestinationArrivalTest {

    private static final int BOARD_SLACK = 60;
    private static final int ACCESS_STOP = 100;
    private static final int ACCESS_DEPARTURE_TIME = 8 * 60 * 60;
    private static final int ACCESS_DURATION_TIME =  72;
    private static final int ACCESS_COST = 420;

    private static final int TRANSIT_1_STOP = 101;
    private static final int TRANSIT_1_BOARD_TIME = ACCESS_DEPARTURE_TIME + 10 * 60;
    private static final int TRANSIT_1_ALIGHT_TIME = TRANSIT_1_BOARD_TIME + 4 * 60;
    private static final TripScheduleInfo A_TRIP = null;

    private static final int DESTINATION_DURATION_TIME = 50;
    private static final int DESTINATION_COST = 500;

    private static final EgressLeg EGRESS_LEG = new EgressLeg() {
        @Override public int stop() { return TRANSIT_1_STOP; }
        @Override public int durationInSeconds() { return DESTINATION_DURATION_TIME; }
        @Override public int cost()  { return DESTINATION_COST; }
    };

    private static final int EXPECTED_ARRIVAL_TIME = TRANSIT_1_ALIGHT_TIME + DESTINATION_DURATION_TIME;
    private static final int EXPECTED_TOTAL_COST = ACCESS_COST + DESTINATION_COST;
    private static final int EXPECTED_TOTAL_DURATION = ACCESS_DURATION_TIME + BOARD_SLACK
            + (TRANSIT_1_ALIGHT_TIME - TRANSIT_1_BOARD_TIME) + DESTINATION_DURATION_TIME;

    private static final TransitCalculator TRANSIT_CALCULATOR = new TransitCalculator(BOARD_SLACK);
    private static final AccessStopArrival<TripScheduleInfo> ACCESS_ARRIVAL = new AccessStopArrival<>(
            ACCESS_STOP, ACCESS_DEPARTURE_TIME, ACCESS_DURATION_TIME, ACCESS_COST, TRANSIT_CALCULATOR
    );

    private static final TransitStopArrival<TripScheduleInfo> TRANSIT_ARRIVAL = new TransitStopArrival<>(
            ACCESS_ARRIVAL, TRANSIT_1_STOP, TRANSIT_1_ALIGHT_TIME, TRANSIT_1_BOARD_TIME, A_TRIP
    );

    private DestinationArrival<TripScheduleInfo> subject = new DestinationArrival<>(TRANSIT_ARRIVAL, EGRESS_LEG);

    @Test
    public void departureTime() {
        assertEquals(TRANSIT_ARRIVAL.arrivalTime(), subject.departureTime());
    }

    @Test
    public void arrivalTime() {
        assertEquals(EXPECTED_ARRIVAL_TIME, subject.arrivalTime());
    }

    @Test
    public void numberOfTransfers() {
        assertEquals(0, subject.numberOfTransfers());
    }

    @Test
    public void travelDuration() {
        assertEquals(EXPECTED_TOTAL_DURATION, subject.travelDuration());
    }

    @Test
    public void cost() {
        assertEquals(EXPECTED_TOTAL_COST, subject.cost());
    }

    @Test
    public void previous() {
        assertEquals(TRANSIT_ARRIVAL, subject.previous());
    }

    @Test
    public void testToString() {
        assertEquals("DestinationArrival { Time: 8:14:50 (0:50), Cost: 920 }", subject.toString());
    }
}