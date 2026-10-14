package com.acme.transport.lastmile.redelivery.policy

import com.acme.transport.lastmile.redelivery.redeliveryquota.PrimaryRedeliveryPolicy
import com.acme.transport.lastmile.redelivery.redeliveryquota.primaryRedeliveryPolicyBucket

fun summarizePrimaryRedeliveryPoliciesForPolicy(items: MutableList<PrimaryRedeliveryPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveFallbackRedeliveryPoliciesFromPolicy(items: MutableList<PrimaryRedeliveryPolicy>): MutableList<FallbackRedeliveryPolicy> {
    val out = mutableListOf<FallbackRedeliveryPolicy>()
    for (item in items) {
        val derived = newFallbackRedeliveryPolicy(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeFallbackRedeliveryPolicy(item: FallbackRedeliveryPolicy): String {
    return primaryRedeliveryPolicyBucket(item.effectiveVolume())
}
