package com.acme.transport.lastmile.proofofdelivery.ledger

import com.acme.transport.lastmile.attempt.manifest.AttemptEstimate
import com.acme.transport.lastmile.attempt.manifest.strictAttemptRecordBucket

fun summarizeAttemptEstimatesForBudget(items: MutableList<AttemptEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveProofOfDeliveryBudgetsFromEstimate(items: MutableList<AttemptEstimate>): MutableList<ProofOfDeliveryBudget> {
    val out = mutableListOf<ProofOfDeliveryBudget>()
    for (item in items) {
        val derived = newProofOfDeliveryBudget(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun mergeProofOfDeliveryBudget(item: ProofOfDeliveryBudget): String {
    return strictAttemptRecordBucket(item.measuredValue())
}
