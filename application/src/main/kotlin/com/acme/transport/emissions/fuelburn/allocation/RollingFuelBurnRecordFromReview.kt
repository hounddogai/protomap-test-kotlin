package com.acme.transport.emissions.fuelburn.allocation

import com.acme.transport.emissions.routeoffset.exception.SeasonalRouteOffsetReview
import com.acme.transport.emissions.routeoffset.exception.seasonalRouteOffsetReviewBucket

fun summarizeSeasonalRouteOffsetReviewsForRecord(items: MutableList<SeasonalRouteOffsetReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveRollingFuelBurnRecordsFromReview(items: MutableList<SeasonalRouteOffsetReview>): MutableList<RollingFuelBurnRecord> {
    val out = mutableListOf<RollingFuelBurnRecord>()
    for (item in items) {
        val derived = newRollingFuelBurnRecord(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankRollingFuelBurnRecord(item: RollingFuelBurnRecord): String {
    return seasonalRouteOffsetReviewBucket(item.adjustedTotal())
}
