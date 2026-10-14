package com.acme.transport.emissions.emissionfactor.quota

import com.acme.transport.emissions.routeoffset.group.CappedRouteOffsetThreshold
import com.acme.transport.emissions.routeoffset.group.normalizeRollingRouteOffsetPlanCode

fun summarizeCappedRouteOffsetThresholdsForEntry(items: MutableList<CappedRouteOffsetThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveEmissionFactorEntriesFromThreshold(items: MutableList<CappedRouteOffsetThreshold>): MutableList<EmissionFactorEntry> {
    val out = mutableListOf<EmissionFactorEntry>()
    for (item in items) {
        val derived = newEmissionFactorEntry(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun matchEmissionFactorEntry(item: EmissionFactorEntry): String = normalizeRollingRouteOffsetPlanCode(item.code)
