package com.example.nhom_15_dulichtphcm.data.ai;

import com.example.nhom_15_dulichtphcm.data.model.Attraction;
import com.example.nhom_15_dulichtphcm.data.model.AttractionCategory;
import com.example.nhom_15_dulichtphcm.data.repository.RideRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Nearest-neighbor geospatial route optimizer and smart multi-day itinerary scheduler.
 */
public final class TripOptimizer {

    private static final int MAX_STOPS_PER_DAY = 5;

    public static final class TripStop {
        public final int orderNumber;
        public final Attraction attraction;
        public final String arrivalClock;
        public final String departureClock;
        public final int stayMinutes;
        public final double legDistanceKm;
        public final int legTravelMinutes;
        public final int estimatedTaxiVnd;

        public TripStop(
                int orderNumber,
                Attraction attraction,
                String arrivalClock,
                String departureClock,
                int stayMinutes,
                double legDistanceKm,
                int legTravelMinutes,
                int estimatedTaxiVnd
        ) {
            this.orderNumber = orderNumber;
            this.attraction = attraction;
            this.arrivalClock = arrivalClock;
            this.departureClock = departureClock;
            this.stayMinutes = stayMinutes;
            this.legDistanceKm = legDistanceKm;
            this.legTravelMinutes = legTravelMinutes;
            this.estimatedTaxiVnd = estimatedTaxiVnd;
        }
    }

    public static final class OptimizedPlan {
        public final List<TripStop> stops;
        public final double totalDistanceKm;
        public final int totalDurationMinutes;
        public final int totalEstimatedTransportVnd;

        public OptimizedPlan(
                List<TripStop> stops,
                double totalDistanceKm,
                int totalDurationMinutes,
                int totalEstimatedTransportVnd
        ) {
            this.stops = Collections.unmodifiableList(new ArrayList<>(stops));
            this.totalDistanceKm = totalDistanceKm;
            this.totalDurationMinutes = totalDurationMinutes;
            this.totalEstimatedTransportVnd = totalEstimatedTransportVnd;
        }
    }

    public static final class DayPlan {
        public final int dayNumber;
        public final OptimizedPlan plan;

        public DayPlan(int dayNumber, OptimizedPlan plan) {
            this.dayNumber = dayNumber;
            this.plan = plan;
        }
    }

    private TripOptimizer() {}

    /**
     * Orders selected attractions using Nearest-Neighbor heuristic starting from the first item
     * and computes arrival/departure timestamps, travel durations, and estimated ride fares.
     */
    public static OptimizedPlan buildPlan(List<Attraction> selected, int startMinutes) {
        if (selected == null || selected.isEmpty()) {
            return new OptimizedPlan(Collections.emptyList(), 0.0, 0, 0);
        }

        List<Attraction> ordered = orderNearestNeighbor(selected);
        List<TripStop> stops = new ArrayList<>(ordered.size());

        int currentClock = Math.max(300, Math.min(22 * 60, startMinutes));
        double totalKm = 0.0;
        int totalTransportVnd = 0;

        for (int i = 0; i < ordered.size(); i++) {
            Attraction cur = ordered.get(i);
            double legKm = 0.0;
            int legMins = 0;
            int legFare = 0;

            if (i > 0) {
                Attraction prev = ordered.get(i - 1);
                legKm = RideRepository.calculateDistanceKm(prev.latitude, prev.longitude, cur.latitude, cur.longitude);
                legMins = Math.max(6, (int) Math.round(legKm * 3.0 + 4.0));
                legFare = RideRepository.estimateFare(RideRepository.providers.get(0), legKm).car4FareVnd;
                currentClock += legMins;
                totalKm += legKm;
                totalTransportVnd += legFare;
            }

            int stayMins = recommendedStayMinutes(cur);
            String arr = formatClock(currentClock);
            currentClock += stayMins;
            String dep = formatClock(currentClock);

            stops.add(new TripStop(
                    i + 1,
                    cur,
                    arr,
                    dep,
                    stayMins,
                    legKm,
                    legMins,
                    legFare
            ));
        }

        int totalMins = Math.max(0, currentClock - startMinutes);
        return new OptimizedPlan(
                stops,
                Math.round(totalKm * 10.0) / 10.0,
                totalMins,
                totalTransportVnd
        );
    }

    public static List<DayPlan> buildMultiDayPlan(List<Attraction> selected, int startMinutes) {
        if (selected == null || selected.isEmpty()) {
            return Collections.emptyList();
        }
        List<Attraction> ordered = orderNearestNeighbor(selected);
        List<DayPlan> days = new ArrayList<>();
        int dayNum = 1;
        for (int idx = 0; idx < ordered.size(); idx += MAX_STOPS_PER_DAY) {
            int end = Math.min(ordered.size(), idx + MAX_STOPS_PER_DAY);
            List<Attraction> slice = ordered.subList(idx, end);
            days.add(new DayPlan(dayNum++, buildPlan(slice, startMinutes)));
        }
        return days;
    }

    public static String formatClock(int minutesFromMidnight) {
        int wrapped = ((minutesFromMidnight % 1440) + 1440) % 1440;
        int hh = wrapped / 60;
        int mm = wrapped % 60;
        return String.format(Locale.US, "%02d:%02d", hh, mm);
    }

    private static List<Attraction> orderNearestNeighbor(List<Attraction> input) {
        List<Attraction> pool = new ArrayList<>(input);
        List<Attraction> result = new ArrayList<>(input.size());
        Attraction current = pool.remove(0);
        result.add(current);

        while (!pool.isEmpty()) {
            int bestIdx = 0;
            double bestDist = Double.MAX_VALUE;
            for (int i = 0; i < pool.size(); i++) {
                Attraction cand = pool.get(i);
                double d = RideRepository.calculateDistanceKm(
                        current.latitude, current.longitude,
                        cand.latitude, cand.longitude
                );
                if (d < bestDist) {
                    bestDist = d;
                    bestIdx = i;
                }
            }
            current = pool.remove(bestIdx);
            result.add(current);
        }
        return result;
    }

    private static int recommendedStayMinutes(Attraction a) {
        if (a.id.contains("cu_chi") || a.id.contains("dai_nam") || a.id.contains("suoi_tien")) {
            return 150;
        }
        if (a.category == AttractionCategory.HISTORICAL || a.category == AttractionCategory.CULTURAL) {
            return 75;
        }
        if (a.category == AttractionCategory.MARKET) {
            return 60;
        }
        return 50;
    }
}
