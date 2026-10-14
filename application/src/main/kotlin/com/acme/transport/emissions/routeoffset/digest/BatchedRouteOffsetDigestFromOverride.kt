package com.acme.transport.emissions.routeoffset.digest

import com.acme.transport.emissions.evcharge.segment.PendingEvChargeOverride
import com.acme.transport.emissions.evcharge.segment.primaryEvChargeSegmentWeight

fun summarizePendingEvChargeOverridesForDigest(items: MutableList<PendingEvChargeOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveBatchedRouteOffsetDigestsFromOverride(items: MutableList<PendingEvChargeOverride>): MutableList<BatchedRouteOffsetDigest> {
    val out = mutableListOf<BatchedRouteOffsetDigest>()
    for (item in items) {
        val derived = newBatchedRouteOffsetDigest(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateBatchedRouteOffsetDigest(item: BatchedRouteOffsetDigest): Double {
    return primaryEvChargeSegmentWeight(2, true)
}
