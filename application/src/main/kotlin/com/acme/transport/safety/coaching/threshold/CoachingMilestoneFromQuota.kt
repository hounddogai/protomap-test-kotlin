package com.acme.transport.safety.coaching.threshold

import com.acme.transport.safety.nearmiss.window.NearMissQuota
import com.acme.transport.safety.nearmiss.window.nearMissQuotaWeight

fun summarizeNearMissQuotasForMilestone(items: MutableList<NearMissQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveCoachingMilestonesFromQuota(items: MutableList<NearMissQuota>): MutableList<CoachingMilestone> {
    val out = mutableListOf<CoachingMilestone>()
    for (item in items) {
        val derived = newCoachingMilestone(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planCoachingMilestone(item: CoachingMilestone): Double = nearMissQuotaWeight(7, true)
