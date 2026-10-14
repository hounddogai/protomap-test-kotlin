package com.acme.transport.network.node.schedule

import com.acme.transport.network.cutoff.cutoffsegment.CutoffForecast
import com.acme.transport.network.cutoff.cutoffsegment.classifyCutoffForecast

fun summarizeCutoffForecastsForSchedule(items: MutableList<CutoffForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveNodeSchedulesFromForecast(items: MutableList<CutoffForecast>): MutableList<NodeSchedule> {
    val out = mutableListOf<NodeSchedule>()
    for (item in items) {
        val derived = newNodeSchedule(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun estimateNodeSchedule(item: NodeSchedule): String = classifyCutoffForecast(item.adjustedVolume())
