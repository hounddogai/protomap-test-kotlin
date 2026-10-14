package com.acme.transport.terminal.sortation.review

import com.acme.transport.terminal.traileryard.schedule.TrailerYardSchedule
import com.acme.transport.terminal.traileryard.schedule.classifyTrailerYardRollup

fun summarizeTrailerYardSchedulesForBundle(items: MutableList<TrailerYardSchedule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveAdaptiveSortationBundlesFromSchedule(items: MutableList<TrailerYardSchedule>): MutableList<AdaptiveSortationBundle> {
    val out = mutableListOf<AdaptiveSortationBundle>()
    for (item in items) {
        val derived = newAdaptiveSortationBundle(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeAdaptiveSortationBundle(item: AdaptiveSortationBundle): String {
    return classifyTrailerYardRollup(item.adjustedLoad())
}
