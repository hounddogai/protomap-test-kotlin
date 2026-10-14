package com.acme.transport.linehaul.tender.forecast

import com.acme.transport.linehaul.relay.batch.RelaySegment
import com.acme.transport.linehaul.relay.batch.relaySegmentWeight

fun summarizeRelaySegmentsForForecast(items: MutableList<RelaySegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun derivePendingTenderForecastsFromSegment(items: MutableList<RelaySegment>): MutableList<PendingTenderForecast> {
    val out = mutableListOf<PendingTenderForecast>()
    for (item in items) {
        val derived = newPendingTenderForecast(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun matchPendingTenderForecast(item: PendingTenderForecast): Double = relaySegmentWeight(3, true)
