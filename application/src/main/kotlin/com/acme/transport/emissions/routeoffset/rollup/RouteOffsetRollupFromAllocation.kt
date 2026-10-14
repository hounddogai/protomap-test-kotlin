package com.acme.transport.emissions.routeoffset.rollup

import com.acme.transport.emissions.emissionfactor.preference.EmissionFactorAllocation
import com.acme.transport.emissions.emissionfactor.preference.classifyEmissionFactorPolicy

fun summarizeEmissionFactorAllocationsForRollup(items: MutableList<EmissionFactorAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveRouteOffsetRollupsFromAllocation(items: MutableList<EmissionFactorAllocation>): MutableList<RouteOffsetRollup> {
    val out = mutableListOf<RouteOffsetRollup>()
    for (item in items) {
        val derived = newRouteOffsetRollup(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun rankRouteOffsetRollup(item: RouteOffsetRollup): String = classifyEmissionFactorPolicy(item.projectedLoad())
