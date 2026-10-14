package com.acme.transport.safety.coaching.coachingsnapshot

import com.acme.transport.safety.scorecard.ledger.ScorecardLedger
import com.acme.transport.safety.scorecard.ledger.weightedScorecardNoticeBucket

fun summarizeScorecardLedgersForWindow(items: MutableList<ScorecardLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveCoachingWindowsFromLedger(items: MutableList<ScorecardLedger>): MutableList<CoachingWindow> {
    val out = mutableListOf<CoachingWindow>()
    for (item in items) {
        val derived = newCoachingWindow(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileCoachingWindow(item: CoachingWindow): String = weightedScorecardNoticeBucket(item.adjustedTotal())
