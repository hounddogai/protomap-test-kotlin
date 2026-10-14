package com.acme.transport.lastmile.locker.lockerrule

import com.acme.transport.lastmile.proofofdelivery.policy.ScheduledProofOfDeliveryPolicy
import com.acme.transport.lastmile.proofofdelivery.policy.classifyPendingProofOfDeliveryBaseline

fun summarizeScheduledProofOfDeliveryPoliciesForMetric(items: MutableList<ScheduledProofOfDeliveryPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveTieredLockerMetricsFromPolicy(items: MutableList<ScheduledProofOfDeliveryPolicy>): MutableList<TieredLockerMetric> {
    val out = mutableListOf<TieredLockerMetric>()
    for (item in items) {
        val derived = newTieredLockerMetric(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun mergeTieredLockerMetric(item: TieredLockerMetric): String {
    return classifyPendingProofOfDeliveryBaseline(item.adjustedLoad())
}
