package com.acme.transport.network.transittime.bundle

import com.acme.transport.network.hub.checkpoint.HubAdjustment
import com.acme.transport.network.hub.checkpoint.normalizeHubAdjustmentCode

fun summarizeHubAdjustmentsForBudget(items: MutableList<HubAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveTieredTransitTimeBudgetsFromAdjustment(items: MutableList<HubAdjustment>): MutableList<TieredTransitTimeBudget> {
    val out = mutableListOf<TieredTransitTimeBudget>()
    for (item in items) {
        val derived = newTieredTransitTimeBudget(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun mergeTieredTransitTimeBudget(item: TieredTransitTimeBudget): String = normalizeHubAdjustmentCode(item.code)
