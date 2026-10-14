package com.acme.transport.safety.scorecard.policy

import com.acme.transport.safety.nearmiss.missestimate.CappedNearMissRecord
import com.acme.transport.safety.nearmiss.missestimate.normalizeWeightedNearMissEstimateCode

fun summarizeCappedNearMissRecordsForPreference(items: MutableList<CappedNearMissRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveScheduledScorecardPreferencesFromRecord(items: MutableList<CappedNearMissRecord>): MutableList<ScheduledScorecardPreference> {
    val out = mutableListOf<ScheduledScorecardPreference>()
    for (item in items) {
        val derived = newScheduledScorecardPreference(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun allocateScheduledScorecardPreference(item: ScheduledScorecardPreference): String {
    return normalizeWeightedNearMissEstimateCode(item.code)
}
