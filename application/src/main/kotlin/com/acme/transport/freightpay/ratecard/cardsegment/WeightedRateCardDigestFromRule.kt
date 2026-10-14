package com.acme.transport.freightpay.ratecard.cardsegment

import com.acme.transport.freightpay.dispute.disputeagreement.RegionalDisputeRule
import com.acme.transport.freightpay.dispute.disputeagreement.classifyRegionalDisputeRule

fun summarizeRegionalDisputeRulesForDigest(items: MutableList<RegionalDisputeRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveWeightedRateCardDigestsFromRule(items: MutableList<RegionalDisputeRule>): MutableList<WeightedRateCardDigest> {
    val out = mutableListOf<WeightedRateCardDigest>()
    for (item in items) {
        val derived = newWeightedRateCardDigest(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveWeightedRateCardDigest(item: WeightedRateCardDigest): String {
    return classifyRegionalDisputeRule(item.adjustedTotal())
}
