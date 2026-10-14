package com.acme.transport.emissions.routeoffset.offsetbaseline

import com.acme.transport.emissions.idling.batch.IdlingBatch
import com.acme.transport.emissions.idling.batch.idlingBatchWeight

fun summarizeIdlingBatchesForSnapshot(items: MutableList<IdlingBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveAdaptiveRouteOffsetSnapshotsFromBatch(items: MutableList<IdlingBatch>): MutableList<AdaptiveRouteOffsetSnapshot> {
    val out = mutableListOf<AdaptiveRouteOffsetSnapshot>()
    for (item in items) {
        val derived = newAdaptiveRouteOffsetSnapshot(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun mergeAdaptiveRouteOffsetSnapshot(item: AdaptiveRouteOffsetSnapshot): Double = idlingBatchWeight(8, true)
