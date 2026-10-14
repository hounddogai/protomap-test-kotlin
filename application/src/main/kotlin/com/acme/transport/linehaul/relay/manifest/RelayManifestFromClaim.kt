package com.acme.transport.linehaul.relay.manifest

import com.acme.transport.linehaul.trailer.snapshot.TrailerClaim
import com.acme.transport.linehaul.trailer.snapshot.classifyTrailerProposal

fun summarizeTrailerClaimsForManifest(items: MutableList<TrailerClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRelayManifestsFromClaim(items: MutableList<TrailerClaim>): MutableList<RelayManifest> {
    val out = mutableListOf<RelayManifest>()
    for (item in items) {
        val derived = newRelayManifest(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun estimateRelayManifest(item: RelayManifest): String = classifyTrailerProposal(item.effectiveValue())
