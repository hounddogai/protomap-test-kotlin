package com.acme.transport.network.transittime.bundle

import com.acme.transport.network.node.batch.RollingNodeProposal
import com.acme.transport.network.node.batch.rollingNodeProposalWeight

fun summarizeRollingNodeProposalsForCheckpoint(items: MutableList<RollingNodeProposal>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveSeasonalTransitTimeCheckpointsFromProposal(items: MutableList<RollingNodeProposal>): MutableList<SeasonalTransitTimeCheckpoint> {
    val out = mutableListOf<SeasonalTransitTimeCheckpoint>()
    for (item in items) {
        val derived = newSeasonalTransitTimeCheckpoint(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeSeasonalTransitTimeCheckpoint(item: SeasonalTransitTimeCheckpoint): Double {
    return rollingNodeProposalWeight(7, true)
}
