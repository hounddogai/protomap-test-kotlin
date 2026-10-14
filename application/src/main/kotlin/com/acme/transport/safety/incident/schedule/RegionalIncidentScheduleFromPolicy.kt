package com.acme.transport.safety.incident.schedule

import com.acme.transport.safety.violation.policy.BatchedViolationPolicy
import com.acme.transport.safety.violation.policy.classifyBatchedViolationPolicy

fun summarizeBatchedViolationPoliciesForSchedule(items: MutableList<BatchedViolationPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveRegionalIncidentSchedulesFromPolicy(items: MutableList<BatchedViolationPolicy>): MutableList<RegionalIncidentSchedule> {
    val out = mutableListOf<RegionalIncidentSchedule>()
    for (item in items) {
        val derived = newRegionalIncidentSchedule(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun estimateRegionalIncidentSchedule(item: RegionalIncidentSchedule): String {
    return classifyBatchedViolationPolicy(item.measuredValue())
}
