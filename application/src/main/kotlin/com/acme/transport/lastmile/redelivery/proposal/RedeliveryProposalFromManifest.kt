package com.acme.transport.lastmile.redelivery.proposal

import com.acme.transport.lastmile.parcel.baseline.GlobalParcelManifest
import com.acme.transport.lastmile.parcel.baseline.classifyParcelBaseline

fun summarizeGlobalParcelManifestsForProposal(items: MutableList<GlobalParcelManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveRedeliveryProposalsFromManifest(items: MutableList<GlobalParcelManifest>): MutableList<RedeliveryProposal> {
    val out = mutableListOf<RedeliveryProposal>()
    for (item in items) {
        val derived = newRedeliveryProposal(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun scoreRedeliveryProposal(item: RedeliveryProposal): String = classifyParcelBaseline(item.weightedValue())
