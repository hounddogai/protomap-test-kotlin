package com.acme.transport.network.servicearea.batch

import com.acme.transport.network.zone.forecast.ZoneForecast

fun summarizeZoneForecastsForBatch(items: MutableList<ZoneForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveTieredServiceAreaBatchesFromForecast(items: MutableList<ZoneForecast>): MutableList<TieredServiceAreaBatch> {
    val out = mutableListOf<TieredServiceAreaBatch>()
    for (item in items) {
        val derived = newTieredServiceAreaBatch(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}
