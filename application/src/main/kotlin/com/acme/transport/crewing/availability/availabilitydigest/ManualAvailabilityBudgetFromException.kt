package com.acme.transport.crewing.availability.availabilitydigest

import com.acme.transport.crewing.restbreak.breakentry.FallbackRestBreakException
import com.acme.transport.crewing.restbreak.breakentry.batchedRestBreakThresholdBucket

fun summarizeFallbackRestBreakExceptionsForBudget(items: MutableList<FallbackRestBreakException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveManualAvailabilityBudgetsFromException(items: MutableList<FallbackRestBreakException>): MutableList<ManualAvailabilityBudget> {
    val out = mutableListOf<ManualAvailabilityBudget>()
    for (item in items) {
        val derived = newManualAvailabilityBudget(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun classifyManualAvailabilityBudget(item: ManualAvailabilityBudget): String {
    return batchedRestBreakThresholdBucket(item.netExposure())
}
