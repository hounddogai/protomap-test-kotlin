package com.acme.transport.emissions.routeoffset.schedule

import com.acme.transport.emissions.routeoffset.digest.BatchedRouteOffsetDigest
import com.acme.transport.emissions.routeoffset.digest.rollingRouteOffsetMilestoneBucket

fun summarizeBatchedRouteOffsetDigestsForThreshold(items: MutableList<BatchedRouteOffsetDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveStrictRouteOffsetThresholdsFromDigest(items: MutableList<BatchedRouteOffsetDigest>): MutableList<StrictRouteOffsetThreshold> {
    val out = mutableListOf<StrictRouteOffsetThreshold>()
    for (item in items) {
        val derived = newStrictRouteOffsetThreshold(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeStrictRouteOffsetThreshold(item: StrictRouteOffsetThreshold): String {
    return rollingRouteOffsetMilestoneBucket(item.effectiveTotal())
}
