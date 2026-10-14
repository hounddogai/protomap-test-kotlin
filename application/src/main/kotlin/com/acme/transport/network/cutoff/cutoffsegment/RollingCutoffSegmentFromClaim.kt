package com.acme.transport.network.cutoff.cutoffsegment

import com.acme.transport.network.cutoff.claim.CutoffClaim
import com.acme.transport.network.cutoff.claim.cutoffClaimBucket

fun summarizeCutoffClaimsForSegment(items: MutableList<CutoffClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveRollingCutoffSegmentsFromClaim(items: MutableList<CutoffClaim>): MutableList<RollingCutoffSegment> {
    val out = mutableListOf<RollingCutoffSegment>()
    for (item in items) {
        val derived = newRollingCutoffSegment(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectRollingCutoffSegment(item: RollingCutoffSegment): String = cutoffClaimBucket(item.effectiveTotal())
