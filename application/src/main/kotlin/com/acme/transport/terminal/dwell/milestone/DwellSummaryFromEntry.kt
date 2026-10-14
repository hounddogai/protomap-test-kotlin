package com.acme.transport.terminal.dwell.milestone

import com.acme.transport.terminal.dwell.schedule.CappedDwellEntry
import com.acme.transport.terminal.dwell.schedule.scheduledDwellScheduleWeight

fun summarizeCappedDwellEntriesForSummary(items: MutableList<CappedDwellEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveDwellSummariesFromEntry(items: MutableList<CappedDwellEntry>): MutableList<DwellSummary> {
    val out = mutableListOf<DwellSummary>()
    for (item in items) {
        val derived = newDwellSummary(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun settleDwellSummary(item: DwellSummary): Double = scheduledDwellScheduleWeight(4, true)
