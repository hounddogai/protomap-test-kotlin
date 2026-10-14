package com.acme.transport.safety.violation.violationpolicy

import com.acme.transport.safety.violation.policy.FallbackViolationAllocation
import com.acme.transport.safety.violation.policy.classifyAdaptiveViolationCycle

fun summarizeFallbackViolationAllocationsForScore(items: MutableList<FallbackViolationAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveAdaptiveViolationScoresFromAllocation(items: MutableList<FallbackViolationAllocation>): MutableList<AdaptiveViolationScore> {
    val out = mutableListOf<AdaptiveViolationScore>()
    for (item in items) {
        val derived = newAdaptiveViolationScore(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceAdaptiveViolationScore(item: AdaptiveViolationScore): String {
    return classifyAdaptiveViolationCycle(item.effectiveExposure())
}
