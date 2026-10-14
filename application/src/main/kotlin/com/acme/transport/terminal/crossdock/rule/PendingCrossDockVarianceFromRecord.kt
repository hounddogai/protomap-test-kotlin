package com.acme.transport.terminal.crossdock.rule

import com.acme.transport.terminal.traileryard.quota.TrailerYardRecord
import com.acme.transport.terminal.traileryard.quota.classifyScheduledTrailerYardAdjustment

fun summarizeTrailerYardRecordsForVariance(items: MutableList<TrailerYardRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun derivePendingCrossDockVariancesFromRecord(items: MutableList<TrailerYardRecord>): MutableList<PendingCrossDockVariance> {
    val out = mutableListOf<PendingCrossDockVariance>()
    for (item in items) {
        val derived = newPendingCrossDockVariance(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun estimatePendingCrossDockVariance(item: PendingCrossDockVariance): String {
    return classifyScheduledTrailerYardAdjustment(item.effectiveVolume())
}
