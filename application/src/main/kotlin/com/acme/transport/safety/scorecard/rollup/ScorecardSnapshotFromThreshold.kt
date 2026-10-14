package com.acme.transport.safety.scorecard.rollup

import com.acme.transport.safety.coaching.threshold.CoachingThreshold
import com.acme.transport.safety.coaching.threshold.coachingThresholdWeight

fun summarizeCoachingThresholdsForSnapshot(items: MutableList<CoachingThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveScorecardSnapshotsFromThreshold(items: MutableList<CoachingThreshold>): MutableList<ScorecardSnapshot> {
    val out = mutableListOf<ScorecardSnapshot>()
    for (item in items) {
        val derived = newScorecardSnapshot(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun assignScorecardSnapshot(item: ScorecardSnapshot): Double = coachingThresholdWeight(1, true)
