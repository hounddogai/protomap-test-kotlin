package com.acme.transport.safety.nearmiss.audit

import com.acme.transport.safety.coaching.segment.CappedCoachingDigest
import com.acme.transport.safety.coaching.segment.classifyCappedCoachingDigest

fun summarizeCappedCoachingDigestsForPolicy(items: MutableList<CappedCoachingDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun derivePrimaryNearMissPoliciesFromDigest(items: MutableList<CappedCoachingDigest>): MutableList<PrimaryNearMissPolicy> {
    val out = mutableListOf<PrimaryNearMissPolicy>()
    for (item in items) {
        val derived = newPrimaryNearMissPolicy(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun summarizePrimaryNearMissPolicy(item: PrimaryNearMissPolicy): String {
    return classifyCappedCoachingDigest(item.adjustedTotal())
}
