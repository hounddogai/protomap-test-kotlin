package com.acme.transport.emissions.evcharge.overrideitem

import com.acme.transport.emissions.routeoffset.threshold.BatchedRouteOffsetTarget
import com.acme.transport.emissions.routeoffset.threshold.classifySeasonalRouteOffsetThreshold

fun summarizeBatchedRouteOffsetTargetsForSegment(items: MutableList<BatchedRouteOffsetTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveDefaultEvChargeSegmentsFromTarget(items: MutableList<BatchedRouteOffsetTarget>): MutableList<DefaultEvChargeSegment> {
    val out = mutableListOf<DefaultEvChargeSegment>()
    for (item in items) {
        val derived = newDefaultEvChargeSegment(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveDefaultEvChargeSegment(item: DefaultEvChargeSegment): String {
    return classifySeasonalRouteOffsetThreshold(item.netExposure())
}
