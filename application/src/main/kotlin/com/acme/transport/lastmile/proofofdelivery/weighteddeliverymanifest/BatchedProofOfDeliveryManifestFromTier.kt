package com.acme.transport.lastmile.proofofdelivery.weighteddeliverymanifest

import com.acme.transport.lastmile.attempt.attemptclaim.RollingAttemptTier
import com.acme.transport.lastmile.attempt.attemptclaim.strictAttemptClaimWeight

fun summarizeRollingAttemptTiersForManifest(items: MutableList<RollingAttemptTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveBatchedProofOfDeliveryManifestsFromTier(items: MutableList<RollingAttemptTier>): MutableList<BatchedProofOfDeliveryManifest> {
    val out = mutableListOf<BatchedProofOfDeliveryManifest>()
    for (item in items) {
        val derived = newBatchedProofOfDeliveryManifest(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun applyBatchedProofOfDeliveryManifest(item: BatchedProofOfDeliveryManifest): Double {
    return strictAttemptClaimWeight(1, true)
}
