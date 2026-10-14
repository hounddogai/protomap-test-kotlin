package com.acme.transport.safety.scorecard.allocation

import com.acme.transport.safety.nearmiss.proposal.LenientNearMissClaim
import com.acme.transport.safety.nearmiss.proposal.classifyCappedNearMissProposal

fun summarizeLenientNearMissClaimsForCheckpoint(items: MutableList<LenientNearMissClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveAdaptiveScorecardCheckpointsFromClaim(items: MutableList<LenientNearMissClaim>): MutableList<AdaptiveScorecardCheckpoint> {
    val out = mutableListOf<AdaptiveScorecardCheckpoint>()
    for (item in items) {
        val derived = newAdaptiveScorecardCheckpoint(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun computeAdaptiveScorecardCheckpoint(item: AdaptiveScorecardCheckpoint): String {
    return classifyCappedNearMissProposal(item.adjustedExposure())
}
