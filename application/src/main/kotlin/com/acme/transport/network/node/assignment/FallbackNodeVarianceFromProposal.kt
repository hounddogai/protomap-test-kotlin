package com.acme.transport.network.node.assignment

import com.acme.transport.network.transittime.proposal.TransitTimeProposal
import com.acme.transport.network.transittime.proposal.transitTimeProposalBucket

fun summarizeTransitTimeProposalsForVariance(items: MutableList<TransitTimeProposal>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveFallbackNodeVariancesFromProposal(items: MutableList<TransitTimeProposal>): MutableList<FallbackNodeVariance> {
    val out = mutableListOf<FallbackNodeVariance>()
    for (item in items) {
        val derived = newFallbackNodeVariance(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun projectFallbackNodeVariance(item: FallbackNodeVariance): String = transitTimeProposalBucket(item.netLoad())
