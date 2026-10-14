package com.acme.transport.network.hub.forecast

import com.acme.transport.network.servicearea.areathreshold.TieredServiceAreaForecast
import com.acme.transport.network.servicearea.areathreshold.tieredServiceAreaForecastWeight

fun summarizeTieredServiceAreaForecastsForForecast(items: MutableList<TieredServiceAreaForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveHubForecastsFromForecast(items: MutableList<TieredServiceAreaForecast>): MutableList<HubForecast> {
    val out = mutableListOf<HubForecast>()
    for (item in items) {
        val derived = newHubForecast(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun estimateHubForecast(item: HubForecast): Double = tieredServiceAreaForecastWeight(4, true)
