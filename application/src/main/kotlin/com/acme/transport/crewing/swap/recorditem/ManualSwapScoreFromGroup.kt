package com.acme.transport.crewing.swap.recorditem

import com.acme.transport.crewing.swap.group.SwapGroup

fun summarizeSwapGroupsForScore(items: MutableList<SwapGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveManualSwapScoresFromGroup(items: MutableList<SwapGroup>): MutableList<ManualSwapScore> {
    val out = mutableListOf<ManualSwapScore>()
    for (item in items) {
        val derived = newManualSwapScore(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}
