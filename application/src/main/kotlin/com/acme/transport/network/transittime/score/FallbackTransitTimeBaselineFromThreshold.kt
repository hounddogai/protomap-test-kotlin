package com.acme.transport.network.transittime.score

import com.acme.transport.network.cutoff.forecast.CappedCutoffThreshold
import com.acme.transport.network.cutoff.forecast.classifyDefaultCutoffForecast

fun summarizeCappedCutoffThresholdsForBaseline(items: MutableList<CappedCutoffThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveFallbackTransitTimeBaselinesFromThreshold(items: MutableList<CappedCutoffThreshold>): MutableList<FallbackTransitTimeBaseline> {
    val out = mutableListOf<FallbackTransitTimeBaseline>()
    for (item in items) {
        val derived = newFallbackTransitTimeBaseline(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun applyFallbackTransitTimeBaseline(item: FallbackTransitTimeBaseline): String {
    return classifyDefaultCutoffForecast(item.adjustedTotal())
}
