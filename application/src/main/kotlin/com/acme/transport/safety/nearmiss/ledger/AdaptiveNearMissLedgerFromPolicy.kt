package com.acme.transport.safety.nearmiss.ledger

import com.acme.transport.safety.violation.policy.BatchedViolationPolicy
import com.acme.transport.safety.violation.policy.batchedViolationPolicyBucket

fun summarizeBatchedViolationPoliciesForLedger(items: MutableList<BatchedViolationPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveAdaptiveNearMissLedgersFromPolicy(items: MutableList<BatchedViolationPolicy>): MutableList<AdaptiveNearMissLedger> {
    val out = mutableListOf<AdaptiveNearMissLedger>()
    for (item in items) {
        val derived = newAdaptiveNearMissLedger(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileAdaptiveNearMissLedger(item: AdaptiveNearMissLedger): String {
    return batchedViolationPolicyBucket(item.effectiveTotal())
}
