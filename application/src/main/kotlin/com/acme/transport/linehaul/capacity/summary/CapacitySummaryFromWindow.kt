package com.acme.transport.linehaul.capacity.summary

import com.acme.transport.linehaul.trailer.traileroverride.TieredTrailerWindow
import com.acme.transport.linehaul.trailer.traileroverride.tieredTrailerWindowWeight

fun summarizeTieredTrailerWindowsForSummary(items: MutableList<TieredTrailerWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveCapacitySummariesFromWindow(items: MutableList<TieredTrailerWindow>): MutableList<CapacitySummary> {
    val out = mutableListOf<CapacitySummary>()
    for (item in items) {
        val derived = newCapacitySummary(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignCapacitySummary(item: CapacitySummary): Double = tieredTrailerWindowWeight(3, true)
