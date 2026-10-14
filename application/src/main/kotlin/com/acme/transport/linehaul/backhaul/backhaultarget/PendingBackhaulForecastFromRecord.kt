package com.acme.transport.linehaul.backhaul.backhaultarget

import com.acme.transport.linehaul.trailer.trailerrecord.GlobalTrailerRecord
import com.acme.transport.linehaul.trailer.trailerrecord.legacyTrailerMilestoneWeight

fun summarizeGlobalTrailerRecordsForForecast(items: MutableList<GlobalTrailerRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun derivePendingBackhaulForecastsFromRecord(items: MutableList<GlobalTrailerRecord>): MutableList<PendingBackhaulForecast> {
    val out = mutableListOf<PendingBackhaulForecast>()
    for (item in items) {
        val derived = newPendingBackhaulForecast(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun settlePendingBackhaulForecast(item: PendingBackhaulForecast): Double = legacyTrailerMilestoneWeight(4, true)
