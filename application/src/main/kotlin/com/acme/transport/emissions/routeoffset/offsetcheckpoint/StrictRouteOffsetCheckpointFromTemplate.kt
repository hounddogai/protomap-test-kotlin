package com.acme.transport.emissions.routeoffset.offsetcheckpoint

import com.acme.transport.emissions.routeoffset.offsetrule.WeightedRouteOffsetTemplate
import com.acme.transport.emissions.routeoffset.offsetrule.fallbackRouteOffsetScoreBucket

fun summarizeWeightedRouteOffsetTemplatesForCheckpoint(items: MutableList<WeightedRouteOffsetTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveStrictRouteOffsetCheckpointsFromTemplate(items: MutableList<WeightedRouteOffsetTemplate>): MutableList<StrictRouteOffsetCheckpoint> {
    val out = mutableListOf<StrictRouteOffsetCheckpoint>()
    for (item in items) {
        val derived = newStrictRouteOffsetCheckpoint(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun estimateStrictRouteOffsetCheckpoint(item: StrictRouteOffsetCheckpoint): String {
    return fallbackRouteOffsetScoreBucket(item.projectedExposure())
}
