package com.acme.transport.emissions.fuelburn.burnnotice

import com.acme.transport.emissions.routeoffset.offsetrule.FallbackRouteOffsetScore
import com.acme.transport.emissions.routeoffset.offsetrule.fallbackRouteOffsetScoreBucket

fun summarizeFallbackRouteOffsetScoresForNotice(items: MutableList<FallbackRouteOffsetScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveRegionalFuelBurnNoticesFromScore(items: MutableList<FallbackRouteOffsetScore>): MutableList<RegionalFuelBurnNotice> {
    val out = mutableListOf<RegionalFuelBurnNotice>()
    for (item in items) {
        val derived = newRegionalFuelBurnNotice(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun computeRegionalFuelBurnNotice(item: RegionalFuelBurnNotice): String {
    return fallbackRouteOffsetScoreBucket(item.weightedLoad())
}
