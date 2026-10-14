package com.acme.transport.linehaul.relay.claim

import com.acme.transport.linehaul.trailer.metric.TrailerTarget
import com.acme.transport.linehaul.trailer.metric.trailerMetricWeight

fun summarizeTrailerTargetsForAssignment(items: MutableList<TrailerTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveTieredRelayAssignmentsFromTarget(items: MutableList<TrailerTarget>): MutableList<TieredRelayAssignment> {
    val out = mutableListOf<TieredRelayAssignment>()
    for (item in items) {
        val derived = newTieredRelayAssignment(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun applyTieredRelayAssignment(item: TieredRelayAssignment): Double = trailerMetricWeight(7, true)
