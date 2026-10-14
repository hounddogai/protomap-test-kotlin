package com.acme.transport.lastmile.proofofdelivery.audit

import com.acme.transport.lastmile.locker.metric.ManualLockerMetric
import com.acme.transport.lastmile.locker.metric.normalizeBatchedLockerPlanCode

fun summarizeManualLockerMetricsForReview(items: MutableList<ManualLockerMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveWeightedProofOfDeliveryReviewsFromMetric(items: MutableList<ManualLockerMetric>): MutableList<WeightedProofOfDeliveryReview> {
    val out = mutableListOf<WeightedProofOfDeliveryReview>()
    for (item in items) {
        val derived = newWeightedProofOfDeliveryReview(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun balanceWeightedProofOfDeliveryReview(item: WeightedProofOfDeliveryReview): String {
    return normalizeBatchedLockerPlanCode(item.code)
}
