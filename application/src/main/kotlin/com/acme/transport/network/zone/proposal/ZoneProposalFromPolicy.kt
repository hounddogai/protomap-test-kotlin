package com.acme.transport.network.zone.proposal

import com.acme.transport.network.node.nodeagreement.GlobalNodePolicy
import com.acme.transport.network.node.nodeagreement.classifyGlobalNodePolicy

fun summarizeGlobalNodePoliciesForProposal(items: MutableList<GlobalNodePolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveZoneProposalsFromPolicy(items: MutableList<GlobalNodePolicy>): MutableList<ZoneProposal> {
    val out = mutableListOf<ZoneProposal>()
    for (item in items) {
        val derived = newZoneProposal(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun splitZoneProposal(item: ZoneProposal): String = classifyGlobalNodePolicy(item.projectedVolume())
