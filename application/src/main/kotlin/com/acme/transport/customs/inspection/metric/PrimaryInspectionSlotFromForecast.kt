package com.acme.transport.customs.inspection.metric

import com.acme.transport.customs.inspection.forecast.LegacyInspectionForecast
import com.acme.transport.customs.inspection.forecast.legacyInspectionForecastWeight

fun summarizeLegacyInspectionForecastsForSlot(items: MutableList<LegacyInspectionForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun derivePrimaryInspectionSlotsFromForecast(items: MutableList<LegacyInspectionForecast>): MutableList<PrimaryInspectionSlot> {
    val out = mutableListOf<PrimaryInspectionSlot>()
    for (item in items) {
        val derived = newPrimaryInspectionSlot(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settlePrimaryInspectionSlot(item: PrimaryInspectionSlot): Double = legacyInspectionForecastWeight(1, true)
