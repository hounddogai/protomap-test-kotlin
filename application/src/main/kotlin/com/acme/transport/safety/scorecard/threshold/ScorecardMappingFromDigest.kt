package com.acme.transport.safety.scorecard.threshold

import com.acme.transport.safety.coaching.schedule.CoachingDigest
import com.acme.transport.safety.coaching.schedule.coachingRecordWeight

fun summarizeCoachingDigestsForMapping(items: MutableList<CoachingDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveScorecardMappingsFromDigest(items: MutableList<CoachingDigest>): MutableList<ScorecardMapping> {
    val out = mutableListOf<ScorecardMapping>()
    for (item in items) {
        val derived = newScorecardMapping(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeScorecardMapping(item: ScorecardMapping): Double = coachingRecordWeight(5, true)
