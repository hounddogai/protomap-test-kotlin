package com.acme.transport.terminal.sortation.variance

import com.acme.transport.terminal.traileryard.budget.TrailerYardMilestone
import com.acme.transport.terminal.traileryard.budget.trailerYardEstimateWeight

fun summarizeTrailerYardMilestonesForRollup(items: MutableList<TrailerYardMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveSortationRollupsFromMilestone(items: MutableList<TrailerYardMilestone>): MutableList<SortationRollup> {
    val out = mutableListOf<SortationRollup>()
    for (item in items) {
        val derived = newSortationRollup(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun matchSortationRollup(item: SortationRollup): Double = trailerYardEstimateWeight(8, true)
