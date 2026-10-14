package com.acme.transport.terminal.traileryard.score

import com.acme.transport.terminal.sortation.policy.RegionalSortationAdjustment
import com.acme.transport.terminal.sortation.policy.normalizeSortationPolicyCode

fun summarizeRegionalSortationAdjustmentsForAllocation(items: MutableList<RegionalSortationAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveWeightedTrailerYardAllocationsFromAdjustment(items: MutableList<RegionalSortationAdjustment>): MutableList<WeightedTrailerYardAllocation> {
    val out = mutableListOf<WeightedTrailerYardAllocation>()
    for (item in items) {
        val derived = newWeightedTrailerYardAllocation(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun estimateWeightedTrailerYardAllocation(item: WeightedTrailerYardAllocation): String {
    return normalizeSortationPolicyCode(item.code)
}
