package com.acme.transport.freightpay.fuelsurcharge.variance

import com.acme.transport.freightpay.dispute.milestone.DisputeMilestone
import com.acme.transport.freightpay.dispute.milestone.disputeGroupBucket

fun summarizeDisputeMilestonesForBundle(items: MutableList<DisputeMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveLenientFuelSurchargeBundlesFromMilestone(items: MutableList<DisputeMilestone>): MutableList<LenientFuelSurchargeBundle> {
    val out = mutableListOf<LenientFuelSurchargeBundle>()
    for (item in items) {
        val derived = newLenientFuelSurchargeBundle(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveLenientFuelSurchargeBundle(item: LenientFuelSurchargeBundle): String {
    return disputeGroupBucket(item.measuredValue())
}
