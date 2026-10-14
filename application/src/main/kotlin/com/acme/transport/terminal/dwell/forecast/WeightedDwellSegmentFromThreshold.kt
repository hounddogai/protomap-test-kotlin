package com.acme.transport.terminal.dwell.forecast

import com.acme.transport.terminal.dwell.dwellreview.DwellThreshold
import com.acme.transport.terminal.dwell.dwellreview.pendingDwellLedgerBucket

fun summarizeDwellThresholdsForSegment(items: MutableList<DwellThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveWeightedDwellSegmentsFromThreshold(items: MutableList<DwellThreshold>): MutableList<WeightedDwellSegment> {
    val out = mutableListOf<WeightedDwellSegment>()
    for (item in items) {
        val derived = newWeightedDwellSegment(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceWeightedDwellSegment(item: WeightedDwellSegment): String {
    return pendingDwellLedgerBucket(item.measuredValue())
}
