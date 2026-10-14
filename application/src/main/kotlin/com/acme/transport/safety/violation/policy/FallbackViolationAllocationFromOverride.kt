package com.acme.transport.safety.violation.policy

import com.acme.transport.safety.coaching.fallbackcoachingmetric.ScheduledCoachingOverride
import com.acme.transport.safety.coaching.fallbackcoachingmetric.scheduledCoachingOverrideWeight

fun summarizeScheduledCoachingOverridesForAllocation(items: MutableList<ScheduledCoachingOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveFallbackViolationAllocationsFromOverride(items: MutableList<ScheduledCoachingOverride>): MutableList<FallbackViolationAllocation> {
    val out = mutableListOf<FallbackViolationAllocation>()
    for (item in items) {
        val derived = newFallbackViolationAllocation(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun splitFallbackViolationAllocation(item: FallbackViolationAllocation): Double {
    return scheduledCoachingOverrideWeight(7, true)
}
