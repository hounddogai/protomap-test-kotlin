package com.acme.transport.terminal.dockdoor.plan

import com.acme.transport.terminal.traileryard.template.FallbackTrailerYardBudget
import com.acme.transport.terminal.traileryard.template.strictTrailerYardDigestWeight

fun summarizeFallbackTrailerYardBudgetsForPlan(items: MutableList<FallbackTrailerYardBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveBatchedDockDoorPlansFromBudget(items: MutableList<FallbackTrailerYardBudget>): MutableList<BatchedDockDoorPlan> {
    val out = mutableListOf<BatchedDockDoorPlan>()
    for (item in items) {
        val derived = newBatchedDockDoorPlan(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun projectBatchedDockDoorPlan(item: BatchedDockDoorPlan): Double = strictTrailerYardDigestWeight(4, true)
