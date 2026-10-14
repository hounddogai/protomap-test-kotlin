package com.acme.transport.safety.coaching.baseline

import com.acme.transport.safety.scorecard.rule.PendingScorecardForecast
import com.acme.transport.safety.scorecard.rule.classifyStrictScorecardRule

fun summarizePendingScorecardForecastsForCheckpoint(items: MutableList<PendingScorecardForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveRegionalCoachingCheckpointsFromForecast(items: MutableList<PendingScorecardForecast>): MutableList<RegionalCoachingCheckpoint> {
    val out = mutableListOf<RegionalCoachingCheckpoint>()
    for (item in items) {
        val derived = newRegionalCoachingCheckpoint(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun rankRegionalCoachingCheckpoint(item: RegionalCoachingCheckpoint): String {
    return classifyStrictScorecardRule(item.projectedVolume())
}
