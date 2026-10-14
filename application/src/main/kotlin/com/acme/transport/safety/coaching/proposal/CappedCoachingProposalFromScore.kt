package com.acme.transport.safety.coaching.proposal

import com.acme.transport.safety.nearmiss.schedule.TieredNearMissScore
import com.acme.transport.safety.nearmiss.schedule.tieredNearMissProfileBucket

fun summarizeTieredNearMissScoresForProposal(items: MutableList<TieredNearMissScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveCappedCoachingProposalsFromScore(items: MutableList<TieredNearMissScore>): MutableList<CappedCoachingProposal> {
    val out = mutableListOf<CappedCoachingProposal>()
    for (item in items) {
        val derived = newCappedCoachingProposal(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun settleCappedCoachingProposal(item: CappedCoachingProposal): String {
    return tieredNearMissProfileBucket(item.netTotal())
}
