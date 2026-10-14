package com.acme.transport.lastmile.redelivery.redeliveryrule

import com.acme.transport.lastmile.proofofdelivery.policy.ScheduledProofOfDeliveryPolicy
import com.acme.transport.lastmile.proofofdelivery.policy.pendingProofOfDeliveryBaselineBucket

fun summarizeScheduledProofOfDeliveryPoliciesForEntry(items: MutableList<ScheduledProofOfDeliveryPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveScheduledRedeliveryEntriesFromPolicy(items: MutableList<ScheduledProofOfDeliveryPolicy>): MutableList<ScheduledRedeliveryEntry> {
    val out = mutableListOf<ScheduledRedeliveryEntry>()
    for (item in items) {
        val derived = newScheduledRedeliveryEntry(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileScheduledRedeliveryEntry(item: ScheduledRedeliveryEntry): String {
    return pendingProofOfDeliveryBaselineBucket(item.measuredValue())
}
