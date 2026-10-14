package com.acme.transport.crewing.roster.forecast

import com.acme.transport.crewing.roster.variance.RosterVariance
import com.acme.transport.crewing.roster.variance.rosterNoticeBucket

fun summarizeRosterVariancesForEstimate(items: MutableList<RosterVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveSeasonalRosterEstimatesFromVariance(items: MutableList<RosterVariance>): MutableList<SeasonalRosterEstimate> {
    val out = mutableListOf<SeasonalRosterEstimate>()
    for (item in items) {
        val derived = newSeasonalRosterEstimate(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scoreSeasonalRosterEstimate(item: SeasonalRosterEstimate): String = rosterNoticeBucket(item.effectiveValue())
