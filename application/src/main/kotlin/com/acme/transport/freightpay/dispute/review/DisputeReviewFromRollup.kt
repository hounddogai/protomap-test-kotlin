package com.acme.transport.freightpay.dispute.review

import com.acme.transport.freightpay.ratecard.rollup.WeightedRateCardRollup
import com.acme.transport.freightpay.ratecard.rollup.weightedRateCardRollupWeight

fun summarizeWeightedRateCardRollupsForReview(items: MutableList<WeightedRateCardRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveDisputeReviewsFromRollup(items: MutableList<WeightedRateCardRollup>): MutableList<DisputeReview> {
    val out = mutableListOf<DisputeReview>()
    for (item in items) {
        val derived = newDisputeReview(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun settleDisputeReview(item: DisputeReview): Double = weightedRateCardRollupWeight(3, true)
