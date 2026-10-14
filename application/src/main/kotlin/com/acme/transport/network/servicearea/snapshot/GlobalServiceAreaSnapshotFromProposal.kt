package com.acme.transport.network.servicearea.snapshot

import com.acme.transport.network.hub.hubplan.AdaptiveHubProposal
import com.acme.transport.network.hub.hubplan.adaptiveHubProposalWeight

fun summarizeAdaptiveHubProposalsForSnapshot(items: MutableList<AdaptiveHubProposal>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveGlobalServiceAreaSnapshotsFromProposal(items: MutableList<AdaptiveHubProposal>): MutableList<GlobalServiceAreaSnapshot> {
    val out = mutableListOf<GlobalServiceAreaSnapshot>()
    for (item in items) {
        val derived = newGlobalServiceAreaSnapshot(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceGlobalServiceAreaSnapshot(item: GlobalServiceAreaSnapshot): Double = adaptiveHubProposalWeight(5, true)
