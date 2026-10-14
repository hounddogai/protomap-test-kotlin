package com.acme.transport.emissions.routeoffset.window

import com.acme.transport.emissions.idling.window.AdaptiveIdlingPolicy
import com.acme.transport.emissions.idling.window.strictIdlingSegmentBucket

fun summarizeAdaptiveIdlingPoliciesForProposal(items: MutableList<AdaptiveIdlingPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveRouteOffsetProposalsFromPolicy(items: MutableList<AdaptiveIdlingPolicy>): MutableList<RouteOffsetProposal> {
    val out = mutableListOf<RouteOffsetProposal>()
    for (item in items) {
        val derived = newRouteOffsetProposal(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun deriveRouteOffsetProposal(item: RouteOffsetProposal): String {
    return strictIdlingSegmentBucket(item.adjustedExposure())
}
