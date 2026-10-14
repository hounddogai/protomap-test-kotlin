package com.acme.transport.lastmile.attempt.attemptmetric

import com.acme.transport.lastmile.parcel.parcelslot.BatchedParcelForecast
import com.acme.transport.lastmile.parcel.parcelslot.classifyDefaultParcelGroup

fun summarizeBatchedParcelForecastsForSummary(items: MutableList<BatchedParcelForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveAttemptSummariesFromForecast(items: MutableList<BatchedParcelForecast>): MutableList<AttemptSummary> {
    val out = mutableListOf<AttemptSummary>()
    for (item in items) {
        val derived = newAttemptSummary(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun estimateAttemptSummary(item: AttemptSummary): String = classifyDefaultParcelGroup(item.effectiveValue())
