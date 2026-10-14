package com.acme.transport.safety.scorecard.schedule

import com.acme.transport.safety.coaching.metric.LegacyCoachingWindow
import com.acme.transport.safety.coaching.metric.normalizeLegacyCoachingWindowCode

fun summarizeLegacyCoachingWindowsForLedger(items: MutableList<LegacyCoachingWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveBatchedScorecardLedgersFromWindow(items: MutableList<LegacyCoachingWindow>): MutableList<BatchedScorecardLedger> {
    val out = mutableListOf<BatchedScorecardLedger>()
    for (item in items) {
        val derived = newBatchedScorecardLedger(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planBatchedScorecardLedger(item: BatchedScorecardLedger): String {
    return normalizeLegacyCoachingWindowCode(item.code)
}
