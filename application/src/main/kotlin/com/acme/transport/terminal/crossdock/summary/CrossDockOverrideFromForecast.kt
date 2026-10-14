package com.acme.transport.terminal.crossdock.summary

import com.acme.transport.terminal.dwell.summary.ManualDwellForecast
import com.acme.transport.terminal.dwell.summary.weightedDwellSummaryWeight

fun summarizeManualDwellForecastsForOverride(items: MutableList<ManualDwellForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveCrossDockOverridesFromForecast(items: MutableList<ManualDwellForecast>): MutableList<CrossDockOverride> {
    val out = mutableListOf<CrossDockOverride>()
    for (item in items) {
        val derived = newCrossDockOverride(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun mergeCrossDockOverride(item: CrossDockOverride): Double = weightedDwellSummaryWeight(2, true)
