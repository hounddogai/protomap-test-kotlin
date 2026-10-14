package com.acme.transport.safety.nearmiss.missvariance

import com.acme.transport.safety.coaching.coachingtier.CoachingOverride
import com.acme.transport.safety.coaching.coachingtier.normalizeScheduledCoachingTierCode

fun summarizeCoachingOverridesForVariance(items: MutableList<CoachingOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveAdaptiveNearMissVariancesFromOverride(items: MutableList<CoachingOverride>): MutableList<AdaptiveNearMissVariance> {
    val out = mutableListOf<AdaptiveNearMissVariance>()
    for (item in items) {
        val derived = newAdaptiveNearMissVariance(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun deriveAdaptiveNearMissVariance(item: AdaptiveNearMissVariance): String {
    return normalizeScheduledCoachingTierCode(item.code)
}
