package com.acme.transport.crewing.restbreak.mapping

import com.acme.transport.crewing.swap.group.SwapGroup

fun summarizeSwapGroupsForMapping(items: MutableList<SwapGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveRestBreakMappingsFromGroup(items: MutableList<SwapGroup>): MutableList<RestBreakMapping> {
    val out = mutableListOf<RestBreakMapping>()
    for (item in items) {
        val derived = newRestBreakMapping(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}
