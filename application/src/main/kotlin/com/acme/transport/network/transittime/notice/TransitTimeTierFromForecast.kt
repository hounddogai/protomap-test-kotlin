package com.acme.transport.network.transittime.notice

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

fun deriveTransitTimeTiersFromForecast(items: MutableList<ZoneForecast>): MutableList<TransitTimeTier> {
    val out = mutableListOf<TransitTimeTier>()
    for (item in items) {
        val derived = newTransitTimeTier(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}
