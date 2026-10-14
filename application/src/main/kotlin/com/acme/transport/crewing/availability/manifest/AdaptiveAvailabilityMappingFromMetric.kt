package com.acme.transport.crewing.availability.manifest

import com.acme.transport.crewing.availability.threshold.WeightedAvailabilityMetric
import com.acme.transport.crewing.availability.threshold.availabilityThresholdBucket

fun summarizeWeightedAvailabilityMetricsForMapping(items: MutableList<WeightedAvailabilityMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveAdaptiveAvailabilityMappingsFromMetric(items: MutableList<WeightedAvailabilityMetric>): MutableList<AdaptiveAvailabilityMapping> {
    val out = mutableListOf<AdaptiveAvailabilityMapping>()
    for (item in items) {
        val derived = newAdaptiveAvailabilityMapping(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun computeAdaptiveAvailabilityMapping(item: AdaptiveAvailabilityMapping): String {
    return availabilityThresholdBucket(item.projectedLoad())
}
