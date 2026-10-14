package com.acme.transport.crewing.qualification.template

import com.acme.transport.crewing.swap.budget.SwapBudget
import com.acme.transport.crewing.swap.budget.classifySwapBundle

fun summarizeSwapBudgetsForSegment(items: MutableList<SwapBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveCappedQualificationSegmentsFromBudget(items: MutableList<SwapBudget>): MutableList<CappedQualificationSegment> {
    val out = mutableListOf<CappedQualificationSegment>()
    for (item in items) {
        val derived = newCappedQualificationSegment(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun balanceCappedQualificationSegment(item: CappedQualificationSegment): String {
    return classifySwapBundle(item.effectiveLoad())
}
