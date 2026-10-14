package com.acme.transport.emissions.routeoffset.baseline

import com.acme.transport.emissions.idling.milestone.IdlingMilestone
import com.acme.transport.emissions.idling.milestone.normalizeIdlingAssignmentCode

fun summarizeIdlingMilestonesForSnapshot(items: MutableList<IdlingMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveRouteOffsetSnapshotsFromMilestone(items: MutableList<IdlingMilestone>): MutableList<RouteOffsetSnapshot> {
    val out = mutableListOf<RouteOffsetSnapshot>()
    for (item in items) {
        val derived = newRouteOffsetSnapshot(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun splitRouteOffsetSnapshot(item: RouteOffsetSnapshot): String = normalizeIdlingAssignmentCode(item.code)
