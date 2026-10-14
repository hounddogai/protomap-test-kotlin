package com.acme.transport.terminal.traileryard.allocation

import com.acme.transport.terminal.sortation.sortationpreference.RollingSortationPreference
import com.acme.transport.terminal.sortation.sortationpreference.rollingSortationPreferenceWeight

fun summarizeRollingSortationPreferencesForAllocation(items: MutableList<RollingSortationPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveTrailerYardAllocationsFromPreference(items: MutableList<RollingSortationPreference>): MutableList<TrailerYardAllocation> {
    val out = mutableListOf<TrailerYardAllocation>()
    for (item in items) {
        val derived = newTrailerYardAllocation(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun applyTrailerYardAllocation(item: TrailerYardAllocation): Double = rollingSortationPreferenceWeight(3, true)
