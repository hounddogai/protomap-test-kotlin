package com.acme.transport.network.cutoff.tier

import com.acme.transport.network.transittime.timerequest.LenientTransitTimeRequest
import com.acme.transport.network.transittime.timerequest.defaultTransitTimeCheckpointWeight

fun summarizeLenientTransitTimeRequestsForRule(items: MutableList<LenientTransitTimeRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveStrictCutoffRulesFromRequest(items: MutableList<LenientTransitTimeRequest>): MutableList<StrictCutoffRule> {
    val out = mutableListOf<StrictCutoffRule>()
    for (item in items) {
        val derived = newStrictCutoffRule(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun deriveStrictCutoffRule(item: StrictCutoffRule): Double = defaultTransitTimeCheckpointWeight(6, true)
