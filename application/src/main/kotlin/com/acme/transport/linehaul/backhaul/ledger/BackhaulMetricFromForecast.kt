package com.acme.transport.linehaul.backhaul.ledger

import com.acme.transport.linehaul.laneplan.forecast.LanePlanForecast
import com.acme.transport.linehaul.laneplan.forecast.tieredLanePlanRequestWeight

fun summarizeLanePlanForecastsForMetric(items: MutableList<LanePlanForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveBackhaulMetricsFromForecast(items: MutableList<LanePlanForecast>): MutableList<BackhaulMetric> {
    val out = mutableListOf<BackhaulMetric>()
    for (item in items) {
        val derived = newBackhaulMetric(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun estimateBackhaulMetric(item: BackhaulMetric): Double = tieredLanePlanRequestWeight(2, true)
