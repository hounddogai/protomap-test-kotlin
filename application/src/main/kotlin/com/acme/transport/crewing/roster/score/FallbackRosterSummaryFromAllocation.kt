package com.acme.transport.crewing.roster.score

import com.acme.transport.crewing.swap.assignment.AdaptiveSwapAllocation
import com.acme.transport.crewing.swap.assignment.batchedSwapAssignmentWeight

fun summarizeAdaptiveSwapAllocationsForSummary(items: MutableList<AdaptiveSwapAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveFallbackRosterSummariesFromAllocation(items: MutableList<AdaptiveSwapAllocation>): MutableList<FallbackRosterSummary> {
    val out = mutableListOf<FallbackRosterSummary>()
    for (item in items) {
        val derived = newFallbackRosterSummary(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleFallbackRosterSummary(item: FallbackRosterSummary): Double = batchedSwapAssignmentWeight(2, true)
