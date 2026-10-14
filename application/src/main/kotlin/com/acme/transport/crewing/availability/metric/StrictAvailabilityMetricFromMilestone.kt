package com.acme.transport.crewing.availability.metric

import com.acme.transport.crewing.dutyperiod.milestone.AdaptiveDutyPeriodMilestone
import com.acme.transport.crewing.dutyperiod.milestone.classifyScheduledDutyPeriodException

fun summarizeAdaptiveDutyPeriodMilestonesForMetric(items: MutableList<AdaptiveDutyPeriodMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveStrictAvailabilityMetricsFromMilestone(items: MutableList<AdaptiveDutyPeriodMilestone>): MutableList<StrictAvailabilityMetric> {
    val out = mutableListOf<StrictAvailabilityMetric>()
    for (item in items) {
        val derived = newStrictAvailabilityMetric(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleStrictAvailabilityMetric(item: StrictAvailabilityMetric): String {
    return classifyScheduledDutyPeriodException(item.adjustedExposure())
}
