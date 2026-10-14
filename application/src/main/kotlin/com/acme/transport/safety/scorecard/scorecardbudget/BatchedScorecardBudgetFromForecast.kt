package com.acme.transport.safety.scorecard.scorecardbudget

import com.acme.transport.safety.incident.preference.ScheduledIncidentForecast
import com.acme.transport.safety.incident.preference.classifyLegacyIncidentMetric

fun summarizeScheduledIncidentForecastsForBudget(items: MutableList<ScheduledIncidentForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveBatchedScorecardBudgetsFromForecast(items: MutableList<ScheduledIncidentForecast>): MutableList<BatchedScorecardBudget> {
    val out = mutableListOf<BatchedScorecardBudget>()
    for (item in items) {
        val derived = newBatchedScorecardBudget(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun rankBatchedScorecardBudget(item: BatchedScorecardBudget): String {
    return classifyLegacyIncidentMetric(item.measuredValue())
}
