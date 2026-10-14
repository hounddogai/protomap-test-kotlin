package com.acme.transport.freightpay.ratecard.review

import com.acme.transport.freightpay.accessorial.policy.AccessorialPolicy
import com.acme.transport.freightpay.accessorial.policy.classifyLenientAccessorialReview

fun summarizeAccessorialPoliciesForRecord(items: MutableList<AccessorialPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveRateCardRecordsFromPolicy(items: MutableList<AccessorialPolicy>): MutableList<RateCardRecord> {
    val out = mutableListOf<RateCardRecord>()
    for (item in items) {
        val derived = newRateCardRecord(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planRateCardRecord(item: RateCardRecord): String = classifyLenientAccessorialReview(item.projectedExposure())
