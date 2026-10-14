package com.acme.transport.linehaul.capacity.ledger

import com.acme.transport.linehaul.trailer.overrideitem.ScheduledTrailerOverride
import com.acme.transport.linehaul.trailer.overrideitem.classifyScheduledTrailerOverride

fun summarizeScheduledTrailerOverridesForEstimate(items: MutableList<ScheduledTrailerOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveSeasonalCapacityEstimatesFromOverride(items: MutableList<ScheduledTrailerOverride>): MutableList<SeasonalCapacityEstimate> {
    val out = mutableListOf<SeasonalCapacityEstimate>()
    for (item in items) {
        val derived = newSeasonalCapacityEstimate(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun splitSeasonalCapacityEstimate(item: SeasonalCapacityEstimate): String {
    return classifyScheduledTrailerOverride(item.effectiveExposure())
}
