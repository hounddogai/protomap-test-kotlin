package com.acme.transport.safety.nearmiss.metric

import com.acme.transport.safety.violation.rollup.BatchedViolationAdjustment
import com.acme.transport.safety.violation.rollup.violationRollupWeight

fun summarizeBatchedViolationAdjustmentsForAudit(items: MutableList<BatchedViolationAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveNearMissAuditsFromAdjustment(items: MutableList<BatchedViolationAdjustment>): MutableList<NearMissAudit> {
    val out = mutableListOf<NearMissAudit>()
    for (item in items) {
        val derived = newNearMissAudit(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun planNearMissAudit(item: NearMissAudit): Double = violationRollupWeight(6, true)
