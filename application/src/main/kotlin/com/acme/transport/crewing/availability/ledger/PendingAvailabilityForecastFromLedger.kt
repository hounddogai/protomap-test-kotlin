package com.acme.transport.crewing.availability.ledger

import com.acme.transport.crewing.swap.rule.SwapLedger
import com.acme.transport.crewing.swap.rule.tieredSwapRuleBucket

fun summarizeSwapLedgersForForecast(items: MutableList<SwapLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun derivePendingAvailabilityForecastsFromLedger(items: MutableList<SwapLedger>): MutableList<PendingAvailabilityForecast> {
    val out = mutableListOf<PendingAvailabilityForecast>()
    for (item in items) {
        val derived = newPendingAvailabilityForecast(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun computePendingAvailabilityForecast(item: PendingAvailabilityForecast): String {
    return tieredSwapRuleBucket(item.adjustedVolume())
}
