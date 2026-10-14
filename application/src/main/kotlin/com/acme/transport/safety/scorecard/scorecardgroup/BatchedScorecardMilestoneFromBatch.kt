package com.acme.transport.safety.scorecard.scorecardgroup

import com.acme.transport.safety.nearmiss.batch.NearMissBatch
import com.acme.transport.safety.nearmiss.batch.normalizeLenientNearMissAgreementCode

fun summarizeNearMissBatchesForMilestone(items: MutableList<NearMissBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveBatchedScorecardMilestonesFromBatch(items: MutableList<NearMissBatch>): MutableList<BatchedScorecardMilestone> {
    val out = mutableListOf<BatchedScorecardMilestone>()
    for (item in items) {
        val derived = newBatchedScorecardMilestone(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeBatchedScorecardMilestone(item: BatchedScorecardMilestone): String {
    return normalizeLenientNearMissAgreementCode(item.code)
}
