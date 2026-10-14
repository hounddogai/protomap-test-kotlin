package com.acme.transport.crewing.restbreak.breakmilestone

import com.acme.transport.crewing.availability.threshold.WeightedAvailabilityMetric
import com.acme.transport.crewing.availability.threshold.availabilityThresholdBucket

fun summarizeWeightedAvailabilityMetricsForMilestone(items: MutableList<WeightedAvailabilityMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveScheduledRestBreakMilestonesFromMetric(items: MutableList<WeightedAvailabilityMetric>): MutableList<ScheduledRestBreakMilestone> {
    val out = mutableListOf<ScheduledRestBreakMilestone>()
    for (item in items) {
        val derived = newScheduledRestBreakMilestone(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun computeScheduledRestBreakMilestone(item: ScheduledRestBreakMilestone): String {
    return availabilityThresholdBucket(item.effectiveTotal())
}
