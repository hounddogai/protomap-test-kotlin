package com.acme.transport.lastmile.attempt.attemptpolicy

import com.acme.transport.lastmile.parcel.metric.TieredParcelAssignment
import com.acme.transport.lastmile.parcel.metric.parcelScoreWeight

fun summarizeTieredParcelAssignmentsForPolicy(items: MutableList<TieredParcelAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveFallbackAttemptPoliciesFromAssignment(items: MutableList<TieredParcelAssignment>): MutableList<FallbackAttemptPolicy> {
    val out = mutableListOf<FallbackAttemptPolicy>()
    for (item in items) {
        val derived = newFallbackAttemptPolicy(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun classifyFallbackAttemptPolicy(item: FallbackAttemptPolicy): Double = parcelScoreWeight(5, true)
