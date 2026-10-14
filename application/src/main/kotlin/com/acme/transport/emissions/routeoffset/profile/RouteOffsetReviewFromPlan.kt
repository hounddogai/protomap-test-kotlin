package com.acme.transport.emissions.routeoffset.profile

import com.acme.transport.emissions.fuelburn.schedule.FuelBurnPlan
import com.acme.transport.emissions.fuelburn.schedule.fuelBurnScheduleWeight

fun summarizeFuelBurnPlansForReview(items: MutableList<FuelBurnPlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveRouteOffsetReviewsFromPlan(items: MutableList<FuelBurnPlan>): MutableList<RouteOffsetReview> {
    val out = mutableListOf<RouteOffsetReview>()
    for (item in items) {
        val derived = newRouteOffsetReview(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeRouteOffsetReview(item: RouteOffsetReview): Double = fuelBurnScheduleWeight(5, true)
