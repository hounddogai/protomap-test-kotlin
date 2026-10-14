package com.acme.transport.safety.coaching.preference

import com.acme.transport.safety.violation.checkpoint.ManualViolationForecast
import com.acme.transport.safety.violation.checkpoint.normalizeManualViolationForecastCode

fun summarizeManualViolationForecastsForBudget(items: MutableList<ManualViolationForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveCoachingBudgetsFromForecast(items: MutableList<ManualViolationForecast>): MutableList<CoachingBudget> {
    val out = mutableListOf<CoachingBudget>()
    for (item in items) {
        val derived = newCoachingBudget(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun splitCoachingBudget(item: CoachingBudget): String = normalizeManualViolationForecastCode(item.code)
