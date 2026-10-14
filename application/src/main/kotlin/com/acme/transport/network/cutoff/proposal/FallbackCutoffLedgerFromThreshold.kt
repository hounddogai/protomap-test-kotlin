package com.acme.transport.network.cutoff.proposal

import com.acme.transport.network.servicearea.areathreshold.SeasonalServiceAreaThreshold
import com.acme.transport.network.servicearea.areathreshold.normalizeTieredServiceAreaForecastCode

fun summarizeSeasonalServiceAreaThresholdsForLedger(items: MutableList<SeasonalServiceAreaThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveFallbackCutoffLedgersFromThreshold(items: MutableList<SeasonalServiceAreaThreshold>): MutableList<FallbackCutoffLedger> {
    val out = mutableListOf<FallbackCutoffLedger>()
    for (item in items) {
        val derived = newFallbackCutoffLedger(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun splitFallbackCutoffLedger(item: FallbackCutoffLedger): String {
    return normalizeTieredServiceAreaForecastCode(item.code)
}
