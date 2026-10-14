package com.acme.transport.emissions.idling.idlingwindow

import com.acme.transport.emissions.routeoffset.offsetestimate.PendingRouteOffsetProfile
import com.acme.transport.emissions.routeoffset.offsetestimate.classifyPendingRouteOffsetProfile

fun summarizePendingRouteOffsetProfilesForThreshold(items: MutableList<PendingRouteOffsetProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveBatchedIdlingThresholdsFromProfile(items: MutableList<PendingRouteOffsetProfile>): MutableList<BatchedIdlingThreshold> {
    val out = mutableListOf<BatchedIdlingThreshold>()
    for (item in items) {
        val derived = newBatchedIdlingThreshold(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun classifyBatchedIdlingThreshold(item: BatchedIdlingThreshold): String {
    return classifyPendingRouteOffsetProfile(item.netTotal())
}
