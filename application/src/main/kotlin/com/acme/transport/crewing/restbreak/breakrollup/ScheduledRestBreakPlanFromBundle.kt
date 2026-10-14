package com.acme.transport.crewing.restbreak.breakrollup

import com.acme.transport.crewing.swap.budget.SwapBundle
import com.acme.transport.crewing.swap.budget.classifySwapBundle

fun summarizeSwapBundlesForPlan(items: MutableList<SwapBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveScheduledRestBreakPlansFromBundle(items: MutableList<SwapBundle>): MutableList<ScheduledRestBreakPlan> {
    val out = mutableListOf<ScheduledRestBreakPlan>()
    for (item in items) {
        val derived = newScheduledRestBreakPlan(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun deriveScheduledRestBreakPlan(item: ScheduledRestBreakPlan): String = classifySwapBundle(item.measuredValue())
