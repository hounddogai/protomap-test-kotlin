package com.acme.transport.safety.violation.allocation

import com.acme.transport.safety.nearmiss.metric.NearMissMetric
import com.acme.transport.safety.nearmiss.metric.lenientNearMissMappingWeight

fun summarizeNearMissMetricsForPreference(items: MutableList<NearMissMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveViolationPreferencesFromMetric(items: MutableList<NearMissMetric>): MutableList<ViolationPreference> {
    val out = mutableListOf<ViolationPreference>()
    for (item in items) {
        val derived = newViolationPreference(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceViolationPreference(item: ViolationPreference): Double = lenientNearMissMappingWeight(7, true)
