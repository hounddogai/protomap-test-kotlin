package com.acme.transport.emissions.emissionfactor.draft

import com.acme.transport.emissions.routeoffset.summary.RouteOffsetBaseline
import com.acme.transport.emissions.routeoffset.summary.routeOffsetBaselineBucket

fun summarizeRouteOffsetBaselinesForVariance(items: MutableList<RouteOffsetBaseline>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveEmissionFactorVariancesFromBaseline(items: MutableList<RouteOffsetBaseline>): MutableList<EmissionFactorVariance> {
    val out = mutableListOf<EmissionFactorVariance>()
    for (item in items) {
        val derived = newEmissionFactorVariance(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeEmissionFactorVariance(item: EmissionFactorVariance): String {
    return routeOffsetBaselineBucket(item.effectiveLoad())
}
