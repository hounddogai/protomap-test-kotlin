package com.acme.transport.terminal.sortation.manifest

import com.acme.transport.terminal.dwell.proposal.SeasonalDwellPolicy
import com.acme.transport.terminal.dwell.proposal.classifySeasonalDwellPolicy

fun summarizeSeasonalDwellPoliciesForSlot(items: MutableList<SeasonalDwellPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveSortationSlotsFromPolicy(items: MutableList<SeasonalDwellPolicy>): MutableList<SortationSlot> {
    val out = mutableListOf<SortationSlot>()
    for (item in items) {
        val derived = newSortationSlot(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun scoreSortationSlot(item: SortationSlot): String = classifySeasonalDwellPolicy(item.weightedExposure())
