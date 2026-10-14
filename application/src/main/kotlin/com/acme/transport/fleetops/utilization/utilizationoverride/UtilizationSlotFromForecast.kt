package com.acme.transport.fleetops.utilization.utilizationoverride

import com.acme.transport.fleetops.utilization.utilizationtarget.UtilizationForecast
import com.acme.transport.fleetops.utilization.utilizationtarget.legacyUtilizationTargetWeight

fun summarizeUtilizationForecastsForSlot(items: MutableList<UtilizationForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveUtilizationSlotsFromForecast(items: MutableList<UtilizationForecast>): MutableList<UtilizationSlot> {
    val out = mutableListOf<UtilizationSlot>()
    for (item in items) {
        val derived = newUtilizationSlot(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scoreUtilizationSlot(item: UtilizationSlot): Double = legacyUtilizationTargetWeight(3, true)
