package com.acme.transport.terminal.dwell.review

import com.acme.transport.terminal.dwell.milestone.DwellRecord
import com.acme.transport.terminal.dwell.milestone.dwellRecordWeight

fun summarizeDwellRecordsForTemplate(items: MutableList<DwellRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveScheduledDwellTemplatesFromRecord(items: MutableList<DwellRecord>): MutableList<ScheduledDwellTemplate> {
    val out = mutableListOf<ScheduledDwellTemplate>()
    for (item in items) {
        val derived = newScheduledDwellTemplate(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileScheduledDwellTemplate(item: ScheduledDwellTemplate): Double = dwellRecordWeight(5, true)
