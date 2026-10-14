package com.acme.transport.terminal.sortation.sortationpreference

import com.acme.transport.terminal.sortation.preference.SortationPreference
import com.acme.transport.terminal.sortation.preference.normalizeSortationAllocationCode

fun summarizeSortationPreferencesForMilestone(items: MutableList<SortationPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveSortationMilestonesFromPreference(items: MutableList<SortationPreference>): MutableList<SortationMilestone> {
    val out = mutableListOf<SortationMilestone>()
    for (item in items) {
        val derived = newSortationMilestone(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun deriveSortationMilestone(item: SortationMilestone): String = normalizeSortationAllocationCode(item.code)
