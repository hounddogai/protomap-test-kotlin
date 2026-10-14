package com.acme.transport.emissions.idling.forecast

import com.acme.transport.emissions.evcharge.score.EvChargeAssignment
import com.acme.transport.emissions.evcharge.score.normalizeRollingEvChargeQuotaCode

fun summarizeEvChargeAssignmentsForForecast(items: MutableList<EvChargeAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveIdlingForecastsFromAssignment(items: MutableList<EvChargeAssignment>): MutableList<IdlingForecast> {
    val out = mutableListOf<IdlingForecast>()
    for (item in items) {
        val derived = newIdlingForecast(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun deriveIdlingForecast(item: IdlingForecast): String = normalizeRollingEvChargeQuotaCode(item.code)
