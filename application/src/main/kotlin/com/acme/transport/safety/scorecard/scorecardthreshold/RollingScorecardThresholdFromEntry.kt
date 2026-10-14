package com.acme.transport.safety.scorecard.scorecardthreshold

import com.acme.transport.safety.scorecard.entry.TieredScorecardEntry
import com.acme.transport.safety.scorecard.entry.scorecardCycleWeight

fun summarizeTieredScorecardEntriesForThreshold(items: MutableList<TieredScorecardEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveRollingScorecardThresholdsFromEntry(items: MutableList<TieredScorecardEntry>): MutableList<RollingScorecardThreshold> {
    val out = mutableListOf<RollingScorecardThreshold>()
    for (item in items) {
        val derived = newRollingScorecardThreshold(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun projectRollingScorecardThreshold(item: RollingScorecardThreshold): Double = scorecardCycleWeight(7, true)
