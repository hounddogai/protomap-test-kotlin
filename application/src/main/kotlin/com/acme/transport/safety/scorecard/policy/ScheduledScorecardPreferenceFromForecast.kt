package com.acme.transport.safety.scorecard.policy

import com.acme.transport.safety.cameraevent.forecast.BatchedCameraEventForecast
import com.acme.transport.safety.cameraevent.forecast.tieredCameraEventTargetWeight

fun summarizeBatchedCameraEventForecastsForPreference(items: MutableList<BatchedCameraEventForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveScheduledScorecardPreferencesFromForecast(items: MutableList<BatchedCameraEventForecast>): MutableList<ScheduledScorecardPreference> {
    val out = mutableListOf<ScheduledScorecardPreference>()
    for (item in items) {
        val derived = newScheduledScorecardPreference(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun classifyScheduledScorecardPreference(item: ScheduledScorecardPreference): Double {
    return tieredCameraEventTargetWeight(6, true)
}
