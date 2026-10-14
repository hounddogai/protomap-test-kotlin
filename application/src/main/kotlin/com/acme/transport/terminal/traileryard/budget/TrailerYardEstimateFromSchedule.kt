package com.acme.transport.terminal.traileryard.budget

import com.acme.transport.terminal.dwell.template.DwellSchedule
import com.acme.transport.terminal.dwell.template.dwellScheduleBucket

fun summarizeDwellSchedulesForEstimate(items: MutableList<DwellSchedule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveTrailerYardEstimatesFromSchedule(items: MutableList<DwellSchedule>): MutableList<TrailerYardEstimate> {
    val out = mutableListOf<TrailerYardEstimate>()
    for (item in items) {
        val derived = newTrailerYardEstimate(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizeTrailerYardEstimate(item: TrailerYardEstimate): String = dwellScheduleBucket(item.effectiveLoad())
