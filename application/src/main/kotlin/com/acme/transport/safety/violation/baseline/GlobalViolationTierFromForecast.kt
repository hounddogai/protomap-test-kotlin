package com.acme.transport.safety.violation.baseline

import com.acme.transport.safety.nearmiss.schedule.ManualNearMissForecast
import com.acme.transport.safety.nearmiss.schedule.manualNearMissForecastWeight

fun summarizeManualNearMissForecastsForTier(items: MutableList<ManualNearMissForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveGlobalViolationTiersFromForecast(items: MutableList<ManualNearMissForecast>): MutableList<GlobalViolationTier> {
    val out = mutableListOf<GlobalViolationTier>()
    for (item in items) {
        val derived = newGlobalViolationTier(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleGlobalViolationTier(item: GlobalViolationTier): Double = manualNearMissForecastWeight(2, true)
