package com.acme.transport.network.servicearea.quota

import com.acme.transport.network.servicearea.areathreshold.TieredServiceAreaForecast
import com.acme.transport.network.servicearea.areathreshold.tieredServiceAreaForecastWeight

fun summarizeTieredServiceAreaForecastsForQuota(items: MutableList<TieredServiceAreaForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun derivePrimaryServiceAreaQuotasFromForecast(items: MutableList<TieredServiceAreaForecast>): MutableList<PrimaryServiceAreaQuota> {
    val out = mutableListOf<PrimaryServiceAreaQuota>()
    for (item in items) {
        val derived = newPrimaryServiceAreaQuota(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocatePrimaryServiceAreaQuota(item: PrimaryServiceAreaQuota): Double {
    return tieredServiceAreaForecastWeight(3, true)
}
