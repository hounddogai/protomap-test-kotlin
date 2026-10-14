package com.acme.transport.crewing.roster.entry

import com.acme.transport.crewing.roster.regionalrostersummary.TieredRosterPlan
import com.acme.transport.crewing.roster.regionalrostersummary.classifyTieredRosterPlan

fun summarizeTieredRosterPlansForCycle(items: MutableList<TieredRosterPlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveBatchedRosterCyclesFromPlan(items: MutableList<TieredRosterPlan>): MutableList<BatchedRosterCycle> {
    val out = mutableListOf<BatchedRosterCycle>()
    for (item in items) {
        val derived = newBatchedRosterCycle(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun balanceBatchedRosterCycle(item: BatchedRosterCycle): String = classifyTieredRosterPlan(item.effectiveVolume())
