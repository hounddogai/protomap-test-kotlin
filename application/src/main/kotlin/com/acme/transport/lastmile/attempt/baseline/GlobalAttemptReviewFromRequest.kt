package com.acme.transport.lastmile.attempt.baseline

import com.acme.transport.lastmile.proofofdelivery.claim.LegacyProofOfDeliveryRequest
import com.acme.transport.lastmile.proofofdelivery.claim.normalizeLegacyProofOfDeliveryRequestCode

fun summarizeLegacyProofOfDeliveryRequestsForReview(items: MutableList<LegacyProofOfDeliveryRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveGlobalAttemptReviewsFromRequest(items: MutableList<LegacyProofOfDeliveryRequest>): MutableList<GlobalAttemptReview> {
    val out = mutableListOf<GlobalAttemptReview>()
    for (item in items) {
        val derived = newGlobalAttemptReview(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun projectGlobalAttemptReview(item: GlobalAttemptReview): String {
    return normalizeLegacyProofOfDeliveryRequestCode(item.code)
}
