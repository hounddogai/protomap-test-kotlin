package com.acme.transport.lastmile.redelivery.baseline

import com.acme.transport.lastmile.attempt.attemptpolicy.FallbackAttemptPolicy
import com.acme.transport.lastmile.attempt.attemptpolicy.normalizeAttemptMetricCode

fun summarizeFallbackAttemptPoliciesForSchedule(items: MutableList<FallbackAttemptPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveRegionalRedeliverySchedulesFromPolicy(items: MutableList<FallbackAttemptPolicy>): MutableList<RegionalRedeliverySchedule> {
    val out = mutableListOf<RegionalRedeliverySchedule>()
    for (item in items) {
        val derived = newRegionalRedeliverySchedule(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun classifyRegionalRedeliverySchedule(item: RegionalRedeliverySchedule): String {
    return normalizeAttemptMetricCode(item.code)
}
