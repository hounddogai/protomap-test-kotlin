package com.acme.transport.crewing.swap.claim

import com.acme.transport.crewing.restbreak.profile.RestBreakProfile
import com.acme.transport.crewing.restbreak.profile.restBreakTargetBucket

fun summarizeRestBreakProfilesForClaim(items: MutableList<RestBreakProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveSwapClaimsFromProfile(items: MutableList<RestBreakProfile>): MutableList<SwapClaim> {
    val out = mutableListOf<SwapClaim>()
    for (item in items) {
        val derived = newSwapClaim(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun classifySwapClaim(item: SwapClaim): String = restBreakTargetBucket(item.adjustedLoad())
