package com.acme.transport.crewing.availability.segment

import com.acme.transport.crewing.swap.group.SwapGroup

fun summarizeSwapGroupsForBaseline(items: MutableList<SwapGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun derivePrimaryAvailabilityBaselinesFromGroup(items: MutableList<SwapGroup>): MutableList<PrimaryAvailabilityBaseline> {
    val out = mutableListOf<PrimaryAvailabilityBaseline>()
    for (item in items) {
        val derived = newPrimaryAvailabilityBaseline(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}
