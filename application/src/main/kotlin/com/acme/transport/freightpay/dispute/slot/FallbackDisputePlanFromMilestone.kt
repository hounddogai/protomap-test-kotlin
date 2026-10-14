package com.acme.transport.freightpay.dispute.slot

import com.acme.transport.freightpay.ratecard.milestone.StrictRateCardMilestone
import com.acme.transport.freightpay.ratecard.milestone.strictRateCardMilestoneBucket

fun summarizeStrictRateCardMilestonesForPlan(items: MutableList<StrictRateCardMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveFallbackDisputePlansFromMilestone(items: MutableList<StrictRateCardMilestone>): MutableList<FallbackDisputePlan> {
    val out = mutableListOf<FallbackDisputePlan>()
    for (item in items) {
        val derived = newFallbackDisputePlan(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleFallbackDisputePlan(item: FallbackDisputePlan): String {
    return strictRateCardMilestoneBucket(item.weightedExposure())
}
