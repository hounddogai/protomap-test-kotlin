package com.acme.transport.customs.inspection.threshold

import com.acme.transport.customs.broker.window.TieredBrokerSchedule
import com.acme.transport.customs.broker.window.normalizePendingBrokerCheckpointCode

fun summarizeTieredBrokerSchedulesForForecast(items: MutableList<TieredBrokerSchedule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveAdaptiveInspectionForecastsFromSchedule(items: MutableList<TieredBrokerSchedule>): MutableList<AdaptiveInspectionForecast> {
    val out = mutableListOf<AdaptiveInspectionForecast>()
    for (item in items) {
        val derived = newAdaptiveInspectionForecast(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeAdaptiveInspectionForecast(item: AdaptiveInspectionForecast): String {
    return normalizePendingBrokerCheckpointCode(item.code)
}
