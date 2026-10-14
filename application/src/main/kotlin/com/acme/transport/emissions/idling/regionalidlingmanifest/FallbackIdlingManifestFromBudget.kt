package com.acme.transport.emissions.idling.regionalidlingmanifest

import com.acme.transport.emissions.routeoffset.offsetassignment.RouteOffsetBudget
import com.acme.transport.emissions.routeoffset.offsetassignment.legacyRouteOffsetAssignmentBucket

fun summarizeRouteOffsetBudgetsForManifest(items: MutableList<RouteOffsetBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveFallbackIdlingManifestsFromBudget(items: MutableList<RouteOffsetBudget>): MutableList<FallbackIdlingManifest> {
    val out = mutableListOf<FallbackIdlingManifest>()
    for (item in items) {
        val derived = newFallbackIdlingManifest(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun computeFallbackIdlingManifest(item: FallbackIdlingManifest): String {
    return legacyRouteOffsetAssignmentBucket(item.effectiveValue())
}
