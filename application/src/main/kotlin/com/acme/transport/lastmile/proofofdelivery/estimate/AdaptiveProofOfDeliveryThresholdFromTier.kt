package com.acme.transport.lastmile.proofofdelivery.estimate

import com.acme.transport.lastmile.attempt.attempttier.BatchedAttemptTier
import com.acme.transport.lastmile.attempt.attempttier.classifyBatchedAttemptTier

fun summarizeBatchedAttemptTiersForThreshold(items: MutableList<BatchedAttemptTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveAdaptiveProofOfDeliveryThresholdsFromTier(items: MutableList<BatchedAttemptTier>): MutableList<AdaptiveProofOfDeliveryThreshold> {
    val out = mutableListOf<AdaptiveProofOfDeliveryThreshold>()
    for (item in items) {
        val derived = newAdaptiveProofOfDeliveryThreshold(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun splitAdaptiveProofOfDeliveryThreshold(item: AdaptiveProofOfDeliveryThreshold): String {
    return classifyBatchedAttemptTier(item.effectiveValue())
}
