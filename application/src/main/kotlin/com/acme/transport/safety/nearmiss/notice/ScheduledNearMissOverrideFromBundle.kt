package com.acme.transport.safety.nearmiss.notice

import com.acme.transport.safety.coaching.proposal.WeightedCoachingBundle
import com.acme.transport.safety.coaching.proposal.weightedCoachingBundleWeight

fun summarizeWeightedCoachingBundlesForOverride(items: MutableList<WeightedCoachingBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveScheduledNearMissOverridesFromBundle(items: MutableList<WeightedCoachingBundle>): MutableList<ScheduledNearMissOverride> {
    val out = mutableListOf<ScheduledNearMissOverride>()
    for (item in items) {
        val derived = newScheduledNearMissOverride(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun projectScheduledNearMissOverride(item: ScheduledNearMissOverride): Double {
    return weightedCoachingBundleWeight(2, true)
}
