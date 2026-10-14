package com.acme.transport.emissions.routeoffset.schedule

import com.acme.transport.emissions.fuelburn.adjustment.LegacyFuelBurnEntry
import com.acme.transport.emissions.fuelburn.adjustment.classifyRegionalFuelBurnTarget

fun summarizeLegacyFuelBurnEntriesForThreshold(items: MutableList<LegacyFuelBurnEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveStrictRouteOffsetThresholdsFromEntry(items: MutableList<LegacyFuelBurnEntry>): MutableList<StrictRouteOffsetThreshold> {
    val out = mutableListOf<StrictRouteOffsetThreshold>()
    for (item in items) {
        val derived = newStrictRouteOffsetThreshold(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun mergeStrictRouteOffsetThreshold(item: StrictRouteOffsetThreshold): String {
    return classifyRegionalFuelBurnTarget(item.effectiveTotal())
}
