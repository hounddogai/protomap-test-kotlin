package com.acme.transport.crewing.swap.budget

import com.acme.transport.crewing.roster.variance.RollingRosterCycle
import com.acme.transport.crewing.roster.variance.rosterNoticeBucket

fun summarizeRollingRosterCyclesForBudget(items: MutableList<RollingRosterCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveSwapBudgetsFromCycle(items: MutableList<RollingRosterCycle>): MutableList<SwapBudget> {
    val out = mutableListOf<SwapBudget>()
    for (item in items) {
        val derived = newSwapBudget(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun applySwapBudget(item: SwapBudget): String = rosterNoticeBucket(item.measuredValue())
