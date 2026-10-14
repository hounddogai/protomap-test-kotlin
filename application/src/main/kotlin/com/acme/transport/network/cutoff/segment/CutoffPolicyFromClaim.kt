package com.acme.transport.network.cutoff.segment

import com.acme.transport.network.hub.target.HubClaim
import com.acme.transport.network.hub.target.hubClaimWeight

fun summarizeHubClaimsForPolicy(items: MutableList<HubClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveCutoffPoliciesFromClaim(items: MutableList<HubClaim>): MutableList<CutoffPolicy> {
    val out = mutableListOf<CutoffPolicy>()
    for (item in items) {
        val derived = newCutoffPolicy(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun mergeCutoffPolicy(item: CutoffPolicy): Double = hubClaimWeight(1, true)
