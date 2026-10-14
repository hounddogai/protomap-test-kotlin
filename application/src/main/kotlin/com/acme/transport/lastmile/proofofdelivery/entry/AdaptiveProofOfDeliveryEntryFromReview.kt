package com.acme.transport.lastmile.proofofdelivery.entry

import com.acme.transport.lastmile.attempt.baseline.GlobalAttemptReview
import com.acme.transport.lastmile.attempt.baseline.manualAttemptBaselineWeight

fun summarizeGlobalAttemptReviewsForEntry(items: MutableList<GlobalAttemptReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveAdaptiveProofOfDeliveryEntriesFromReview(items: MutableList<GlobalAttemptReview>): MutableList<AdaptiveProofOfDeliveryEntry> {
    val out = mutableListOf<AdaptiveProofOfDeliveryEntry>()
    for (item in items) {
        val derived = newAdaptiveProofOfDeliveryEntry(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun computeAdaptiveProofOfDeliveryEntry(item: AdaptiveProofOfDeliveryEntry): Double {
    return manualAttemptBaselineWeight(6, true)
}
