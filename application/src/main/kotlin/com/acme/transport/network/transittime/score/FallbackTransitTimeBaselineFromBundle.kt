package com.acme.transport.network.transittime.score

import com.acme.transport.network.node.ledger.SeasonalNodeBundle
import com.acme.transport.network.node.ledger.cappedNodeCheckpointWeight

fun summarizeSeasonalNodeBundlesForBaseline(items: MutableList<SeasonalNodeBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveFallbackTransitTimeBaselinesFromBundle(items: MutableList<SeasonalNodeBundle>): MutableList<FallbackTransitTimeBaseline> {
    val out = mutableListOf<FallbackTransitTimeBaseline>()
    for (item in items) {
        val derived = newFallbackTransitTimeBaseline(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun deriveFallbackTransitTimeBaseline(item: FallbackTransitTimeBaseline): Double {
    return cappedNodeCheckpointWeight(3, true)
}
