package com.acme.transport.lastmile.proofofdelivery.audit

import com.acme.transport.lastmile.attempt.threshold.AttemptThreshold
import com.acme.transport.lastmile.attempt.threshold.classifySeasonalAttemptLedger

fun summarizeAttemptThresholdsForOverride(items: MutableList<AttemptThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveProofOfDeliveryOverridesFromThreshold(items: MutableList<AttemptThreshold>): MutableList<ProofOfDeliveryOverride> {
    val out = mutableListOf<ProofOfDeliveryOverride>()
    for (item in items) {
        val derived = newProofOfDeliveryOverride(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun splitProofOfDeliveryOverride(item: ProofOfDeliveryOverride): String {
    return classifySeasonalAttemptLedger(item.weightedExposure())
}
