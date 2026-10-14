package com.acme.transport.lastmile.drop.dropreservation

import com.acme.transport.lastmile.drop.forecast.TieredDropTemplate
import com.acme.transport.lastmile.drop.forecast.fallbackDropForecastBucket

fun summarizeTieredDropTemplatesForForecast(items: MutableList<TieredDropTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveScheduledDropForecastsFromTemplate(items: MutableList<TieredDropTemplate>): MutableList<ScheduledDropForecast> {
    val out = mutableListOf<ScheduledDropForecast>()
    for (item in items) {
        val derived = newScheduledDropForecast(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun planScheduledDropForecast(item: ScheduledDropForecast): String {
    return fallbackDropForecastBucket(item.projectedVolume())
}
