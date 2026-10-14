package com.acme.transport.freightpay.accessorial.accessorialmetric

import com.acme.transport.freightpay.ratecard.policy.WeightedRateCardThreshold
import com.acme.transport.freightpay.ratecard.policy.weightedRateCardThresholdBucket

fun summarizeWeightedRateCardThresholdsForReview(items: MutableList<WeightedRateCardThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveScheduledAccessorialReviewsFromThreshold(items: MutableList<WeightedRateCardThreshold>): MutableList<ScheduledAccessorialReview> {
    val out = mutableListOf<ScheduledAccessorialReview>()
    for (item in items) {
        val derived = newScheduledAccessorialReview(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun assignScheduledAccessorialReview(item: ScheduledAccessorialReview): String {
    return weightedRateCardThresholdBucket(item.effectiveExposure())
}
