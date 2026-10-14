package com.acme.transport.crewing.swap.request

import com.acme.transport.crewing.availability.threshold.WeightedAvailabilityMetric
import com.acme.transport.crewing.availability.threshold.seasonalAvailabilityTemplateBucket

fun summarizeWeightedAvailabilityMetricsForRequest(items: MutableList<WeightedAvailabilityMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveScheduledSwapRequestsFromMetric(items: MutableList<WeightedAvailabilityMetric>): MutableList<ScheduledSwapRequest> {
    val out = mutableListOf<ScheduledSwapRequest>()
    for (item in items) {
        val derived = newScheduledSwapRequest(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateScheduledSwapRequest(item: ScheduledSwapRequest): String {
    return seasonalAvailabilityTemplateBucket(item.effectiveValue())
}
