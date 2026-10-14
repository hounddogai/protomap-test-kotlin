package com.acme.transport.terminal.sortation.milestone

import com.acme.transport.terminal.sortation.sortationbaseline.RegionalSortationClaim
import com.acme.transport.terminal.sortation.sortationbaseline.classifyRegionalSortationClaim

fun summarizeRegionalSortationClaimsForWindow(items: MutableList<RegionalSortationClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveSortationWindowsFromClaim(items: MutableList<RegionalSortationClaim>): MutableList<SortationWindow> {
    val out = mutableListOf<SortationWindow>()
    for (item in items) {
        val derived = newSortationWindow(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun allocateSortationWindow(item: SortationWindow): String = classifyRegionalSortationClaim(item.weightedTotal())
