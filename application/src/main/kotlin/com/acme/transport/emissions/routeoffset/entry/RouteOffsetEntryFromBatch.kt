package com.acme.transport.emissions.routeoffset.entry

import com.acme.transport.emissions.emissionfactor.recorditem.ScheduledEmissionFactorBatch
import com.acme.transport.emissions.emissionfactor.recorditem.tieredEmissionFactorScheduleWeight

fun summarizeScheduledEmissionFactorBatchesForEntry(items: MutableList<ScheduledEmissionFactorBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveRouteOffsetEntriesFromBatch(items: MutableList<ScheduledEmissionFactorBatch>): MutableList<RouteOffsetEntry> {
    val out = mutableListOf<RouteOffsetEntry>()
    for (item in items) {
        val derived = newRouteOffsetEntry(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun deriveRouteOffsetEntry(item: RouteOffsetEntry): Double = tieredEmissionFactorScheduleWeight(7, true)
