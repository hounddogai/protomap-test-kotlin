package com.acme.transport.emissions.evcharge.chargescore

import com.acme.transport.emissions.routeoffset.segment.LenientRouteOffsetSegment
import com.acme.transport.emissions.routeoffset.segment.classifyRouteOffsetPlan

fun summarizeLenientRouteOffsetSegmentsForVariance(items: MutableList<LenientRouteOffsetSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveAdaptiveEvChargeVariancesFromSegment(items: MutableList<LenientRouteOffsetSegment>): MutableList<AdaptiveEvChargeVariance> {
    val out = mutableListOf<AdaptiveEvChargeVariance>()
    for (item in items) {
        val derived = newAdaptiveEvChargeVariance(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeAdaptiveEvChargeVariance(item: AdaptiveEvChargeVariance): String {
    return classifyRouteOffsetPlan(item.measuredValue())
}
