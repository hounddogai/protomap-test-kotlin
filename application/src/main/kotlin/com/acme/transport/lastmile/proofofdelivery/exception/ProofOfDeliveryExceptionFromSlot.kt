package com.acme.transport.lastmile.proofofdelivery.exception

import com.acme.transport.lastmile.attempt.attemptdigest.AttemptSlot
import com.acme.transport.lastmile.attempt.attemptdigest.weightedAttemptDigestWeight

fun summarizeAttemptSlotsForException(items: MutableList<AttemptSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveProofOfDeliveryExceptionsFromSlot(items: MutableList<AttemptSlot>): MutableList<ProofOfDeliveryException> {
    val out = mutableListOf<ProofOfDeliveryException>()
    for (item in items) {
        val derived = newProofOfDeliveryException(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun deriveProofOfDeliveryException(item: ProofOfDeliveryException): Double = weightedAttemptDigestWeight(7, true)
