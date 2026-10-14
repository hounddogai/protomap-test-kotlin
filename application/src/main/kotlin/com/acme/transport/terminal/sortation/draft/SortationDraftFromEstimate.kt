package com.acme.transport.terminal.sortation.draft

import com.acme.transport.terminal.dockdoor.rule.DockDoorEstimate
import com.acme.transport.terminal.dockdoor.rule.dockDoorEstimateBucket

fun summarizeDockDoorEstimatesForDraft(items: MutableList<DockDoorEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveSortationDraftsFromEstimate(items: MutableList<DockDoorEstimate>): MutableList<SortationDraft> {
    val out = mutableListOf<SortationDraft>()
    for (item in items) {
        val derived = newSortationDraft(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun computeSortationDraft(item: SortationDraft): String = dockDoorEstimateBucket(item.weightedValue())
