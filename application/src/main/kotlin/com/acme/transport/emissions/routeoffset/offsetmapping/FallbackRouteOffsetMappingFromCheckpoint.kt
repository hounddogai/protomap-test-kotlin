package com.acme.transport.emissions.routeoffset.offsetmapping

import com.acme.transport.emissions.idling.checkpoint.PrimaryIdlingCheckpoint
import com.acme.transport.emissions.idling.checkpoint.idlingDigestWeight

fun summarizePrimaryIdlingCheckpointsForMapping(items: MutableList<PrimaryIdlingCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveFallbackRouteOffsetMappingsFromCheckpoint(items: MutableList<PrimaryIdlingCheckpoint>): MutableList<FallbackRouteOffsetMapping> {
    val out = mutableListOf<FallbackRouteOffsetMapping>()
    for (item in items) {
        val derived = newFallbackRouteOffsetMapping(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun estimateFallbackRouteOffsetMapping(item: FallbackRouteOffsetMapping): Double = idlingDigestWeight(4, true)
