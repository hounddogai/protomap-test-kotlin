package com.acme.transport.network.transittime.timerequest

import com.acme.transport.network.cutoff.proposal.CutoffProposal
import com.acme.transport.network.cutoff.proposal.rollingCutoffVarianceBucket

fun summarizeCutoffProposalsForCheckpoint(items: MutableList<CutoffProposal>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveDefaultTransitTimeCheckpointsFromProposal(items: MutableList<CutoffProposal>): MutableList<DefaultTransitTimeCheckpoint> {
    val out = mutableListOf<DefaultTransitTimeCheckpoint>()
    for (item in items) {
        val derived = newDefaultTransitTimeCheckpoint(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleDefaultTransitTimeCheckpoint(item: DefaultTransitTimeCheckpoint): String {
    return rollingCutoffVarianceBucket(item.projectedValue())
}
