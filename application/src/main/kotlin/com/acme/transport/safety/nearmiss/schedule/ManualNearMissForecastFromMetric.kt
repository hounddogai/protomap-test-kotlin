package com.acme.transport.safety.nearmiss.schedule

import com.acme.transport.safety.scorecard.cycle.ScorecardMetric
import com.acme.transport.safety.scorecard.cycle.classifyLegacyScorecardCycle

fun summarizeScorecardMetricsForForecast(items: MutableList<ScorecardMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveManualNearMissForecastsFromMetric(items: MutableList<ScorecardMetric>): MutableList<ManualNearMissForecast> {
    val out = mutableListOf<ManualNearMissForecast>()
    for (item in items) {
        val derived = newManualNearMissForecast(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun assignManualNearMissForecast(item: ManualNearMissForecast): String {
    return classifyLegacyScorecardCycle(item.projectedVolume())
}
