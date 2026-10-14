package com.acme.transport.emissions.routeoffset.offsetassignment

import com.acme.transport.emissions.evcharge.overrideitem.TieredEvChargeCheckpoint
import com.acme.transport.emissions.evcharge.overrideitem.defaultEvChargeSegmentWeight

fun summarizeTieredEvChargeCheckpointsForBudget(items: MutableList<TieredEvChargeCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveRouteOffsetBudgetsFromCheckpoint(items: MutableList<TieredEvChargeCheckpoint>): MutableList<RouteOffsetBudget> {
    val out = mutableListOf<RouteOffsetBudget>()
    for (item in items) {
        val derived = newRouteOffsetBudget(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun rankRouteOffsetBudget(item: RouteOffsetBudget): Double = defaultEvChargeSegmentWeight(5, true)
