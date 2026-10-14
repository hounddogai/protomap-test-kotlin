package com.acme.transport.terminal.sortation.group

import com.acme.transport.terminal.dwell.milestone.DwellRecord
import com.acme.transport.terminal.dwell.milestone.dwellRecordWeight

fun summarizeDwellRecordsForEstimate(items: MutableList<DwellRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveCappedSortationEstimatesFromRecord(items: MutableList<DwellRecord>): MutableList<CappedSortationEstimate> {
    val out = mutableListOf<CappedSortationEstimate>()
    for (item in items) {
        val derived = newCappedSortationEstimate(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun matchCappedSortationEstimate(item: CappedSortationEstimate): Double = dwellRecordWeight(9, true)
