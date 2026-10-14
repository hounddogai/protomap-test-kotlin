package com.acme.transport.safety.scorecard.snapshot

import com.acme.transport.safety.coaching.entry.TieredCoachingSchedule
import com.acme.transport.safety.coaching.entry.classifyStrictCoachingEntry

fun summarizeTieredCoachingSchedulesForVariance(items: MutableList<TieredCoachingSchedule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveLegacyScorecardVariancesFromSchedule(items: MutableList<TieredCoachingSchedule>): MutableList<LegacyScorecardVariance> {
    val out = mutableListOf<LegacyScorecardVariance>()
    for (item in items) {
        val derived = newLegacyScorecardVariance(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveLegacyScorecardVariance(item: LegacyScorecardVariance): String {
    return classifyStrictCoachingEntry(item.weightedValue())
}
