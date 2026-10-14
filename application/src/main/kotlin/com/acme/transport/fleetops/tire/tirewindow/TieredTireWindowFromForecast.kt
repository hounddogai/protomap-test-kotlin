package com.acme.transport.fleetops.tire.tirewindow

import com.acme.transport.fleetops.serviceplan.metric.LegacyServicePlanForecast
import com.acme.transport.fleetops.serviceplan.metric.normalizeLegacyServicePlanForecastCode

fun summarizeLegacyServicePlanForecastsForWindow(items: MutableList<LegacyServicePlanForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveTieredTireWindowsFromForecast(items: MutableList<LegacyServicePlanForecast>): MutableList<TieredTireWindow> {
    val out = mutableListOf<TieredTireWindow>()
    for (item in items) {
        val derived = newTieredTireWindow(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun balanceTieredTireWindow(item: TieredTireWindow): String = normalizeLegacyServicePlanForecastCode(item.code)
