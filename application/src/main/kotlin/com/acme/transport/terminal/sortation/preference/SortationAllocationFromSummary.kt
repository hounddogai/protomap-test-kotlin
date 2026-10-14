package com.acme.transport.terminal.sortation.preference

import com.acme.transport.terminal.dockdoor.exception.PrimaryDockDoorSummary
import com.acme.transport.terminal.dockdoor.exception.classifyPrimaryDockDoorSummary

fun summarizePrimaryDockDoorSummariesForAllocation(items: MutableList<PrimaryDockDoorSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveSortationAllocationsFromSummary(items: MutableList<PrimaryDockDoorSummary>): MutableList<SortationAllocation> {
    val out = mutableListOf<SortationAllocation>()
    for (item in items) {
        val derived = newSortationAllocation(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun splitSortationAllocation(item: SortationAllocation): String {
    return classifyPrimaryDockDoorSummary(item.projectedExposure())
}
