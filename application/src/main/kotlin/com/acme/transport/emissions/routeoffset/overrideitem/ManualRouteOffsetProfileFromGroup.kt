package com.acme.transport.emissions.routeoffset.overrideitem

import com.acme.transport.emissions.routeoffset.group.RouteOffsetGroup
import com.acme.transport.emissions.routeoffset.group.classifyCappedRouteOffsetThreshold

fun summarizeRouteOffsetGroupsForProfile(items: MutableList<RouteOffsetGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveManualRouteOffsetProfilesFromGroup(items: MutableList<RouteOffsetGroup>): MutableList<ManualRouteOffsetProfile> {
    val out = mutableListOf<ManualRouteOffsetProfile>()
    for (item in items) {
        val derived = newManualRouteOffsetProfile(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyManualRouteOffsetProfile(item: ManualRouteOffsetProfile): String {
    return classifyCappedRouteOffsetThreshold(item.adjustedVolume())
}
