package com.acme.transport.safety.scorecard.snapshot

import com.acme.transport.safety.scorecard.scorecardwindow.BatchedScorecardThreshold
import com.acme.transport.safety.scorecard.scorecardwindow.normalizeBatchedScorecardThresholdCode

fun summarizeBatchedScorecardThresholdsForSnapshot(items: MutableList<BatchedScorecardThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveScheduledScorecardSnapshotsFromThreshold(items: MutableList<BatchedScorecardThreshold>): MutableList<ScheduledScorecardSnapshot> {
    val out = mutableListOf<ScheduledScorecardSnapshot>()
    for (item in items) {
        val derived = newScheduledScorecardSnapshot(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun scoreScheduledScorecardSnapshot(item: ScheduledScorecardSnapshot): String {
    return normalizeBatchedScorecardThresholdCode(item.code)
}
