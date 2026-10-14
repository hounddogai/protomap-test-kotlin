package com.acme.transport.crewing.dutyperiod.target

import com.acme.transport.crewing.swap.budget.SwapBudget
import com.acme.transport.crewing.swap.budget.classifySwapBundle

fun summarizeSwapBudgetsForTarget(items: MutableList<SwapBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveStrictDutyPeriodTargetsFromBudget(items: MutableList<SwapBudget>): MutableList<StrictDutyPeriodTarget> {
    val out = mutableListOf<StrictDutyPeriodTarget>()
    for (item in items) {
        val derived = newStrictDutyPeriodTarget(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun settleStrictDutyPeriodTarget(item: StrictDutyPeriodTarget): String = classifySwapBundle(item.adjustedVolume())
