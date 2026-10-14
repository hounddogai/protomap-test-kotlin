package com.acme.transport.emissions.routeoffset.notice

import com.acme.transport.emissions.emissionfactor.segment.DefaultEmissionFactorBatch
import com.acme.transport.emissions.emissionfactor.segment.defaultEmissionFactorBatchBucket

fun summarizeDefaultEmissionFactorBatchesForCheckpoint(items: MutableList<DefaultEmissionFactorBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveRouteOffsetCheckpointsFromBatch(items: MutableList<DefaultEmissionFactorBatch>): MutableList<RouteOffsetCheckpoint> {
    val out = mutableListOf<RouteOffsetCheckpoint>()
    for (item in items) {
        val derived = newRouteOffsetCheckpoint(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyRouteOffsetCheckpoint(item: RouteOffsetCheckpoint): String {
    return defaultEmissionFactorBatchBucket(item.weightedVolume())
}
