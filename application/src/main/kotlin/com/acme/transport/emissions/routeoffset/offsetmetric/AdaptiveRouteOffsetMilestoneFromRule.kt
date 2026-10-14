package com.acme.transport.emissions.routeoffset.offsetmetric

import com.acme.transport.emissions.routeoffset.claim.FallbackRouteOffsetRule
import com.acme.transport.emissions.routeoffset.claim.classifyAdaptiveRouteOffsetScore

fun summarizeFallbackRouteOffsetRulesForMilestone(items: MutableList<FallbackRouteOffsetRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveAdaptiveRouteOffsetMilestonesFromRule(items: MutableList<FallbackRouteOffsetRule>): MutableList<AdaptiveRouteOffsetMilestone> {
    val out = mutableListOf<AdaptiveRouteOffsetMilestone>()
    for (item in items) {
        val derived = newAdaptiveRouteOffsetMilestone(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyAdaptiveRouteOffsetMilestone(item: AdaptiveRouteOffsetMilestone): String {
    return classifyAdaptiveRouteOffsetScore(item.projectedTotal())
}
