package com.acme.transport.network.transittime.mapping

import com.acme.transport.network.hub.batchedhubmilestone.AdaptiveHubMilestone
import com.acme.transport.network.hub.batchedhubmilestone.lenientHubThresholdWeight

fun summarizeAdaptiveHubMilestonesForMapping(items: MutableList<AdaptiveHubMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveTransitTimeMappingsFromMilestone(items: MutableList<AdaptiveHubMilestone>): MutableList<TransitTimeMapping> {
    val out = mutableListOf<TransitTimeMapping>()
    for (item in items) {
        val derived = newTransitTimeMapping(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeTransitTimeMapping(item: TransitTimeMapping): Double = lenientHubThresholdWeight(1, true)
