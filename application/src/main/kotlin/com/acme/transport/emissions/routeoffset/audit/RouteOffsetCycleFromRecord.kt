package com.acme.transport.emissions.routeoffset.audit

import com.acme.transport.emissions.fuelburn.recorditem.FuelBurnRecord
import com.acme.transport.emissions.fuelburn.recorditem.primaryFuelBurnNoticeWeight

fun summarizeFuelBurnRecordsForCycle(items: MutableList<FuelBurnRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveRouteOffsetCyclesFromRecord(items: MutableList<FuelBurnRecord>): MutableList<RouteOffsetCycle> {
    val out = mutableListOf<RouteOffsetCycle>()
    for (item in items) {
        val derived = newRouteOffsetCycle(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeRouteOffsetCycle(item: RouteOffsetCycle): Double = primaryFuelBurnNoticeWeight(6, true)
