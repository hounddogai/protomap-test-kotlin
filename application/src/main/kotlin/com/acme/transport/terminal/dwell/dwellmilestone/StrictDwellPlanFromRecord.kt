package com.acme.transport.terminal.dwell.dwellmilestone

import com.acme.transport.terminal.sortation.baseline.LegacySortationRecord
import com.acme.transport.terminal.sortation.baseline.sortationBaselineBucket

fun summarizeLegacySortationRecordsForPlan(items: MutableList<LegacySortationRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveStrictDwellPlansFromRecord(items: MutableList<LegacySortationRecord>): MutableList<StrictDwellPlan> {
    val out = mutableListOf<StrictDwellPlan>()
    for (item in items) {
        val derived = newStrictDwellPlan(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileStrictDwellPlan(item: StrictDwellPlan): String = sortationBaselineBucket(item.adjustedValue())
