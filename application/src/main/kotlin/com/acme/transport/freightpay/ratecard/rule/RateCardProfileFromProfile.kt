package com.acme.transport.freightpay.ratecard.rule

import com.acme.transport.freightpay.dispute.variance.GlobalDisputeProfile
import com.acme.transport.freightpay.dispute.variance.fallbackDisputeCheckpointWeight

fun summarizeGlobalDisputeProfilesForProfile(items: MutableList<GlobalDisputeProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveRateCardProfilesFromProfile(items: MutableList<GlobalDisputeProfile>): MutableList<RateCardProfile> {
    val out = mutableListOf<RateCardProfile>()
    for (item in items) {
        val derived = newRateCardProfile(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun splitRateCardProfile(item: RateCardProfile): Double = fallbackDisputeCheckpointWeight(2, true)
