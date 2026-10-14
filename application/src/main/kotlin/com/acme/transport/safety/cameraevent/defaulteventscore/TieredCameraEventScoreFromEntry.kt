package com.acme.transport.safety.cameraevent.defaulteventscore

import com.acme.transport.safety.scorecard.entry.TieredScorecardEntry
import com.acme.transport.safety.scorecard.entry.scorecardCycleBucket

fun summarizeTieredScorecardEntriesForScore(items: MutableList<TieredScorecardEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveTieredCameraEventScoresFromEntry(items: MutableList<TieredScorecardEntry>): MutableList<TieredCameraEventScore> {
    val out = mutableListOf<TieredCameraEventScore>()
    for (item in items) {
        val derived = newTieredCameraEventScore(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun planTieredCameraEventScore(item: TieredCameraEventScore): String = scorecardCycleBucket(item.netExposure())
