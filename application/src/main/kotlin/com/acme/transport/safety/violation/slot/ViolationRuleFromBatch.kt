package com.acme.transport.safety.violation.slot

import com.acme.transport.safety.incident.batch.GlobalIncidentBatch
import com.acme.transport.safety.incident.batch.normalizeGlobalIncidentBatchCode

fun summarizeGlobalIncidentBatchesForRule(items: MutableList<GlobalIncidentBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveViolationRulesFromBatch(items: MutableList<GlobalIncidentBatch>): MutableList<ViolationRule> {
    val out = mutableListOf<ViolationRule>()
    for (item in items) {
        val derived = newViolationRule(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun settleViolationRule(item: ViolationRule): String = normalizeGlobalIncidentBatchCode(item.code)
