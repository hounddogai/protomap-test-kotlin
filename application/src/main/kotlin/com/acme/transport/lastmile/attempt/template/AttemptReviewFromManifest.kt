package com.acme.transport.lastmile.attempt.template

import com.acme.transport.lastmile.proofofdelivery.manifest.WeightedProofOfDeliveryManifest
import com.acme.transport.lastmile.proofofdelivery.manifest.defaultProofOfDeliveryRuleWeight

fun summarizeWeightedProofOfDeliveryManifestsForReview(items: MutableList<WeightedProofOfDeliveryManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveAttemptReviewsFromManifest(items: MutableList<WeightedProofOfDeliveryManifest>): MutableList<AttemptReview> {
    val out = mutableListOf<AttemptReview>()
    for (item in items) {
        val derived = newAttemptReview(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun assignAttemptReview(item: AttemptReview): Double = defaultProofOfDeliveryRuleWeight(3, true)
