package com.acme.transport.safety.scorecard.scorecardwindow

import com.acme.transport.safety.violation.violationpolicy.SeasonalViolationClaim
import com.acme.transport.safety.violation.violationpolicy.classifyGlobalViolationProposal

fun summarizeSeasonalViolationClaimsForThreshold(items: MutableList<SeasonalViolationClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveBatchedScorecardThresholdsFromClaim(items: MutableList<SeasonalViolationClaim>): MutableList<BatchedScorecardThreshold> {
    val out = mutableListOf<BatchedScorecardThreshold>()
    for (item in items) {
        val derived = newBatchedScorecardThreshold(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun computeBatchedScorecardThreshold(item: BatchedScorecardThreshold): String {
    return classifyGlobalViolationProposal(item.weightedVolume())
}
