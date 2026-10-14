package com.acme.transport.crewing.roster.rosteragreement

import com.acme.transport.crewing.availability.threshold.WeightedAvailabilityMetric
import com.acme.transport.crewing.availability.threshold.classifyAvailabilityThreshold

fun summarizeWeightedAvailabilityMetricsForNotice(items: MutableList<WeightedAvailabilityMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveCappedRosterNoticesFromMetric(items: MutableList<WeightedAvailabilityMetric>): MutableList<CappedRosterNotice> {
    val out = mutableListOf<CappedRosterNotice>()
    for (item in items) {
        val derived = newCappedRosterNotice(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizeCappedRosterNotice(item: CappedRosterNotice): String {
    return classifyAvailabilityThreshold(item.adjustedVolume())
}
