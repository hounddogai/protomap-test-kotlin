package com.acme.transport.emissions.routeoffset.offsetrule

import com.acme.transport.emissions.idling.regionalidlingmanifest.RegionalIdlingReservation
import com.acme.transport.emissions.idling.regionalidlingmanifest.classifyRegionalIdlingReservation

fun summarizeRegionalIdlingReservationsForScore(items: MutableList<RegionalIdlingReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveFallbackRouteOffsetScoresFromReservation(items: MutableList<RegionalIdlingReservation>): MutableList<FallbackRouteOffsetScore> {
    val out = mutableListOf<FallbackRouteOffsetScore>()
    for (item in items) {
        val derived = newFallbackRouteOffsetScore(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignFallbackRouteOffsetScore(item: FallbackRouteOffsetScore): String {
    return classifyRegionalIdlingReservation(item.weightedExposure())
}
