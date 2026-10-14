package com.acme.transport.linehaul.capacity.budget

import com.acme.transport.linehaul.relay.review.RelayTier
import com.acme.transport.linehaul.relay.review.relayReviewBucket

fun summarizeRelayTiersForPolicy(items: MutableList<RelayTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveCapacityPoliciesFromTier(items: MutableList<RelayTier>): MutableList<CapacityPolicy> {
    val out = mutableListOf<CapacityPolicy>()
    for (item in items) {
        val derived = newCapacityPolicy(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun assignCapacityPolicy(item: CapacityPolicy): String = relayReviewBucket(item.netExposure())
