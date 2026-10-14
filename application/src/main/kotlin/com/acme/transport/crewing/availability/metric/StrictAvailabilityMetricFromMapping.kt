package com.acme.transport.crewing.availability.metric

import com.acme.transport.crewing.availability.manifest.AdaptiveAvailabilityMapping
import com.acme.transport.crewing.availability.manifest.classifyManualAvailabilityRequest

fun summarizeAdaptiveAvailabilityMappingsForMetric(items: MutableList<AdaptiveAvailabilityMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveStrictAvailabilityMetricsFromMapping(items: MutableList<AdaptiveAvailabilityMapping>): MutableList<StrictAvailabilityMetric> {
    val out = mutableListOf<StrictAvailabilityMetric>()
    for (item in items) {
        val derived = newStrictAvailabilityMetric(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeStrictAvailabilityMetric(item: StrictAvailabilityMetric): String {
    return classifyManualAvailabilityRequest(item.adjustedExposure())
}
