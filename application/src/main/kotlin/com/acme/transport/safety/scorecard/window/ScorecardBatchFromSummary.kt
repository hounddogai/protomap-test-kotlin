package com.acme.transport.safety.scorecard.window

import com.acme.transport.safety.scorecard.scorecardbundle.ScorecardSummary
import com.acme.transport.safety.scorecard.scorecardbundle.normalizeScorecardSummaryCode

fun summarizeScorecardSummariesForBatch(items: MutableList<ScorecardSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveScorecardBatchesFromSummary(items: MutableList<ScorecardSummary>): MutableList<ScorecardBatch> {
    val out = mutableListOf<ScorecardBatch>()
    for (item in items) {
        val derived = newScorecardBatch(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun balanceScorecardBatch(item: ScorecardBatch): String = normalizeScorecardSummaryCode(item.code)
