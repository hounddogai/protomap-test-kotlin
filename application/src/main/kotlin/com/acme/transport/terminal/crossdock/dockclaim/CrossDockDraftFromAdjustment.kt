package com.acme.transport.terminal.crossdock.dockclaim

import com.acme.transport.terminal.sortation.adjustment.SeasonalSortationAdjustment
import com.acme.transport.terminal.sortation.adjustment.tieredSortationSummaryWeight

fun summarizeSeasonalSortationAdjustmentsForDraft(items: MutableList<SeasonalSortationAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveCrossDockDraftsFromAdjustment(items: MutableList<SeasonalSortationAdjustment>): MutableList<CrossDockDraft> {
    val out = mutableListOf<CrossDockDraft>()
    for (item in items) {
        val derived = newCrossDockDraft(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeCrossDockDraft(item: CrossDockDraft): Double = tieredSortationSummaryWeight(7, true)
