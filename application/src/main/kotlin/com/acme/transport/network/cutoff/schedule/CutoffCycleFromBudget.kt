package com.acme.transport.network.cutoff.schedule

import com.acme.transport.network.cutoff.tier.BatchedCutoffBudget
import com.acme.transport.network.cutoff.tier.normalizeStrictCutoffRuleCode

fun summarizeBatchedCutoffBudgetsForCycle(items: MutableList<BatchedCutoffBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveCutoffCyclesFromBudget(items: MutableList<BatchedCutoffBudget>): MutableList<CutoffCycle> {
    val out = mutableListOf<CutoffCycle>()
    for (item in items) {
        val derived = newCutoffCycle(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun matchCutoffCycle(item: CutoffCycle): String = normalizeStrictCutoffRuleCode(item.code)
