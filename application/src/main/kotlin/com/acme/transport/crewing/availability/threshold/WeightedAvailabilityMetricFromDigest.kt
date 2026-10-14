package com.acme.transport.crewing.availability.threshold

import com.acme.transport.crewing.qualification.tieredqualificationdigest.RollingQualificationDigest
import com.acme.transport.crewing.qualification.tieredqualificationdigest.regionalQualificationNoticeBucket

fun summarizeRollingQualificationDigestsForMetric(items: MutableList<RollingQualificationDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveWeightedAvailabilityMetricsFromDigest(items: MutableList<RollingQualificationDigest>): MutableList<WeightedAvailabilityMetric> {
    val out = mutableListOf<WeightedAvailabilityMetric>()
    for (item in items) {
        val derived = newWeightedAvailabilityMetric(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun estimateWeightedAvailabilityMetric(item: WeightedAvailabilityMetric): String {
    return regionalQualificationNoticeBucket(item.weightedLoad())
}
