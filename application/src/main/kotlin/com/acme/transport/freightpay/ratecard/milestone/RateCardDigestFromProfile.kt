package com.acme.transport.freightpay.ratecard.milestone

import com.acme.transport.freightpay.dispute.metric.DisputeProfile
import com.acme.transport.freightpay.dispute.metric.normalizeRollingDisputePolicyCode

fun summarizeDisputeProfilesForDigest(items: MutableList<DisputeProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveRateCardDigestsFromProfile(items: MutableList<DisputeProfile>): MutableList<RateCardDigest> {
    val out = mutableListOf<RateCardDigest>()
    for (item in items) {
        val derived = newRateCardDigest(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeRateCardDigest(item: RateCardDigest): String = normalizeRollingDisputePolicyCode(item.code)
