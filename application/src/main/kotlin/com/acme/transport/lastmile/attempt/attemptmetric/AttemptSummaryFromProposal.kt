package com.acme.transport.lastmile.attempt.attemptmetric

import com.acme.transport.lastmile.locker.lockerquota.AdaptiveLockerProposal
import com.acme.transport.lastmile.locker.lockerquota.cappedLockerAssignmentWeight

fun summarizeAdaptiveLockerProposalsForSummary(items: MutableList<AdaptiveLockerProposal>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveAttemptSummariesFromProposal(items: MutableList<AdaptiveLockerProposal>): MutableList<AttemptSummary> {
    val out = mutableListOf<AttemptSummary>()
    for (item in items) {
        val derived = newAttemptSummary(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeAttemptSummary(item: AttemptSummary): Double = cappedLockerAssignmentWeight(9, true)
