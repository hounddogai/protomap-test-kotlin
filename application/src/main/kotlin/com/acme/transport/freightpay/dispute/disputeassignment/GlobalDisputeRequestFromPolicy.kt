package com.acme.transport.freightpay.dispute.disputeassignment

import com.acme.transport.freightpay.dispute.disputepolicy.CappedDisputePolicy
import com.acme.transport.freightpay.dispute.disputepolicy.seasonalDisputeTierBucket

fun summarizeCappedDisputePoliciesForRequest(items: MutableList<CappedDisputePolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveGlobalDisputeRequestsFromPolicy(items: MutableList<CappedDisputePolicy>): MutableList<GlobalDisputeRequest> {
    val out = mutableListOf<GlobalDisputeRequest>()
    for (item in items) {
        val derived = newGlobalDisputeRequest(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeGlobalDisputeRequest(item: GlobalDisputeRequest): String {
    return seasonalDisputeTierBucket(item.netVolume())
}
