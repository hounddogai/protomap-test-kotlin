package com.acme.transport.lastmile.proofofdelivery.reservation

import com.acme.transport.lastmile.attempt.bundle.AttemptTier
import com.acme.transport.lastmile.attempt.bundle.classifyAttemptPlan

fun summarizeAttemptTiersForMilestone(items: MutableList<AttemptTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveProofOfDeliveryMilestonesFromTier(items: MutableList<AttemptTier>): MutableList<ProofOfDeliveryMilestone> {
    val out = mutableListOf<ProofOfDeliveryMilestone>()
    for (item in items) {
        val derived = newProofOfDeliveryMilestone(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignProofOfDeliveryMilestone(item: ProofOfDeliveryMilestone): String {
    return classifyAttemptPlan(item.weightedLoad())
}
