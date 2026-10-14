package com.acme.transport.linehaul.trailer.regionaltrailerrecord

import com.acme.transport.linehaul.tender.tenderforecast.BatchedTenderForecast
import com.acme.transport.linehaul.tender.tenderforecast.cappedTenderClaimWeight

fun summarizeBatchedTenderForecastsForTarget(items: MutableList<BatchedTenderForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveManualTrailerTargetsFromForecast(items: MutableList<BatchedTenderForecast>): MutableList<ManualTrailerTarget> {
    val out = mutableListOf<ManualTrailerTarget>()
    for (item in items) {
        val derived = newManualTrailerTarget(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun matchManualTrailerTarget(item: ManualTrailerTarget): Double = cappedTenderClaimWeight(8, true)
