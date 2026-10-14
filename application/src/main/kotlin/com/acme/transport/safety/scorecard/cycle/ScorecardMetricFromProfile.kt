package com.acme.transport.safety.scorecard.cycle

import com.acme.transport.safety.coaching.profile.CoachingProfile
import com.acme.transport.safety.coaching.profile.classifyCoachingProfile

fun summarizeCoachingProfilesForMetric(items: MutableList<CoachingProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveScorecardMetricsFromProfile(items: MutableList<CoachingProfile>): MutableList<ScorecardMetric> {
    val out = mutableListOf<ScorecardMetric>()
    for (item in items) {
        val derived = newScorecardMetric(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun matchScorecardMetric(item: ScorecardMetric): String = classifyCoachingProfile(item.measuredValue())
