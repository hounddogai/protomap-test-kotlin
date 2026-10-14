package com.acme.transport.network.transittime.scheduledtimebatch

import com.acme.transport.network.transittime.threshold.TransitTimeThreshold
import com.acme.transport.network.transittime.threshold.classifyScheduledTransitTimeEntry

fun summarizeTransitTimeThresholdsForBatch(items: MutableList<TransitTimeThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveScheduledTransitTimeBatchesFromThreshold(items: MutableList<TransitTimeThreshold>): MutableList<ScheduledTransitTimeBatch> {
    val out = mutableListOf<ScheduledTransitTimeBatch>()
    for (item in items) {
        val derived = newScheduledTransitTimeBatch(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankScheduledTransitTimeBatch(item: ScheduledTransitTimeBatch): String {
    return classifyScheduledTransitTimeEntry(item.effectiveExposure())
}
