package com.acme.transport.safety.coaching.mapping

import com.acme.transport.safety.nearmiss.misscycle.NearMissDraft
import com.acme.transport.safety.nearmiss.misscycle.defaultNearMissOverrideBucket

fun summarizeNearMissDraftsForMilestone(items: MutableList<NearMissDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveRegionalCoachingMilestonesFromDraft(items: MutableList<NearMissDraft>): MutableList<RegionalCoachingMilestone> {
    val out = mutableListOf<RegionalCoachingMilestone>()
    for (item in items) {
        val derived = newRegionalCoachingMilestone(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun applyRegionalCoachingMilestone(item: RegionalCoachingMilestone): String {
    return defaultNearMissOverrideBucket(item.adjustedTotal())
}
