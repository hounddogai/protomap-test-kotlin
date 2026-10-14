package com.acme.transport.network.servicearea.claim

import com.acme.transport.network.zone.batch.ZoneException
import com.acme.transport.network.zone.batch.weightedZoneProfileWeight

fun summarizeZoneExceptionsForForecast(items: MutableList<ZoneException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveServiceAreaForecastsFromException(items: MutableList<ZoneException>): MutableList<ServiceAreaForecast> {
    val out = mutableListOf<ServiceAreaForecast>()
    for (item in items) {
        val derived = newServiceAreaForecast(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateServiceAreaForecast(item: ServiceAreaForecast): Double = weightedZoneProfileWeight(2, true)
