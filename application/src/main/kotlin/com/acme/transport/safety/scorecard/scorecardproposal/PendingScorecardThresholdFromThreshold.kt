package com.acme.transport.safety.scorecard.scorecardproposal

import com.acme.transport.safety.violation.snapshot.CappedViolationThreshold
import com.acme.transport.safety.violation.snapshot.classifyGlobalViolationAllocation

fun summarizeCappedViolationThresholdsForThreshold(items: MutableList<CappedViolationThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun derivePendingScorecardThresholdsFromThreshold(items: MutableList<CappedViolationThreshold>): MutableList<PendingScorecardThreshold> {
    val out = mutableListOf<PendingScorecardThreshold>()
    for (item in items) {
        val derived = newPendingScorecardThreshold(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun applyPendingScorecardThreshold(item: PendingScorecardThreshold): String {
    return classifyGlobalViolationAllocation(item.projectedExposure())
}
