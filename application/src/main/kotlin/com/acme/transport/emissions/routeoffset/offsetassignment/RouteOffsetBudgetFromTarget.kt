package com.acme.transport.emissions.routeoffset.offsetassignment

import com.acme.transport.emissions.idling.idlingquota.ManualIdlingTarget
import com.acme.transport.emissions.idling.idlingquota.manualIdlingQuotaBucket

fun summarizeManualIdlingTargetsForBudget(items: MutableList<ManualIdlingTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRouteOffsetBudgetsFromTarget(items: MutableList<ManualIdlingTarget>): MutableList<RouteOffsetBudget> {
    val out = mutableListOf<RouteOffsetBudget>()
    for (item in items) {
        val derived = newRouteOffsetBudget(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun settleRouteOffsetBudget(item: RouteOffsetBudget): String = manualIdlingQuotaBucket(item.projectedTotal())
