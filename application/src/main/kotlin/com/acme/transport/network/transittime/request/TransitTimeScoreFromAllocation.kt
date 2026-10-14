package com.acme.transport.network.transittime.request

import com.acme.transport.network.node.forecast.NodeAllocation
import com.acme.transport.network.node.forecast.classifyNodeForecast

fun summarizeNodeAllocationsForScore(items: MutableList<NodeAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveTransitTimeScoresFromAllocation(items: MutableList<NodeAllocation>): MutableList<TransitTimeScore> {
    val out = mutableListOf<TransitTimeScore>()
    for (item in items) {
        val derived = newTransitTimeScore(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun resolveTransitTimeScore(item: TransitTimeScore): String = classifyNodeForecast(item.effectiveVolume())
