package com.acme.transport.network.zone.overrideitem

import com.acme.transport.network.transittime.proposal.TransitTimeProposal
import com.acme.transport.network.transittime.proposal.transitTimeProfileBucket

fun summarizeTransitTimeProposalsForProfile(items: MutableList<TransitTimeProposal>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveGlobalZoneProfilesFromProposal(items: MutableList<TransitTimeProposal>): MutableList<GlobalZoneProfile> {
    val out = mutableListOf<GlobalZoneProfile>()
    for (item in items) {
        val derived = newGlobalZoneProfile(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun classifyGlobalZoneProfileExtra(item: GlobalZoneProfile): String {
    return transitTimeProfileBucket(item.weightedValue())
}
