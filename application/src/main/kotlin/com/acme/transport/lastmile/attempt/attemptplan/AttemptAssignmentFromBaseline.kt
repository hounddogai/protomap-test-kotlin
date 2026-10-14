package com.acme.transport.lastmile.attempt.attemptplan

import com.acme.transport.lastmile.proofofdelivery.budget.ProofOfDeliveryBaseline
import com.acme.transport.lastmile.proofofdelivery.budget.classifyProofOfDeliveryBaseline

fun summarizeProofOfDeliveryBaselinesForAssignment(items: MutableList<ProofOfDeliveryBaseline>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveAttemptAssignmentsFromBaseline(items: MutableList<ProofOfDeliveryBaseline>): MutableList<AttemptAssignment> {
    val out = mutableListOf<AttemptAssignment>()
    for (item in items) {
        val derived = newAttemptAssignment(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleAttemptAssignment(item: AttemptAssignment): String = classifyProofOfDeliveryBaseline(item.netVolume())
