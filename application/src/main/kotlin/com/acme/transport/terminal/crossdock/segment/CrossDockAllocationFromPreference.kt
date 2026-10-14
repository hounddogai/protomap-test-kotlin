package com.acme.transport.terminal.crossdock.segment

import com.acme.transport.terminal.sortation.preference.SortationPreference
import com.acme.transport.terminal.sortation.preference.classifySortationPreference

fun summarizeSortationPreferencesForAllocation(items: MutableList<SortationPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveCrossDockAllocationsFromPreference(items: MutableList<SortationPreference>): MutableList<CrossDockAllocation> {
    val out = mutableListOf<CrossDockAllocation>()
    for (item in items) {
        val derived = newCrossDockAllocation(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun applyCrossDockAllocation(item: CrossDockAllocation): String = classifySortationPreference(item.netLoad())
