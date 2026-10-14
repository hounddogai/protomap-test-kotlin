package com.acme.transport.network.node.schedule

import com.acme.transport.network.zone.forecast.ZoneForecast

fun summarizeZoneForecastsForTier(items: MutableList<ZoneForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveNodeTiersFromForecast(items: MutableList<ZoneForecast>): MutableList<NodeTier> {
    val out = mutableListOf<NodeTier>()
    for (item in items) {
        val derived = newNodeTier(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}
