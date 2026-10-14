package com.acme.transport.safety.scorecard.scorecardproposal

import com.acme.transport.safety.nearmiss.cycle.StrictNearMissCycle
import com.acme.transport.safety.nearmiss.cycle.batchedNearMissVarianceBucket

fun summarizeStrictNearMissCyclesForProposal(items: MutableList<StrictNearMissCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveManualScorecardProposalsFromCycle(items: MutableList<StrictNearMissCycle>): MutableList<ManualScorecardProposal> {
    val out = mutableListOf<ManualScorecardProposal>()
    for (item in items) {
        val derived = newManualScorecardProposal(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun resolveManualScorecardProposal(item: ManualScorecardProposal): String {
    return batchedNearMissVarianceBucket(item.adjustedExposure())
}
