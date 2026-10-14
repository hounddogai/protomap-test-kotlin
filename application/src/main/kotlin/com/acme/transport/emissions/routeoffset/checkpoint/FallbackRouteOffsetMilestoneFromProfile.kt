package com.acme.transport.emissions.routeoffset.checkpoint

import com.acme.transport.emissions.idling.idlingforecast.IdlingProfile
import com.acme.transport.emissions.idling.idlingforecast.classifyIdlingProfile

fun summarizeIdlingProfilesForMilestone(items: MutableList<IdlingProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveFallbackRouteOffsetMilestonesFromProfile(items: MutableList<IdlingProfile>): MutableList<FallbackRouteOffsetMilestone> {
    val out = mutableListOf<FallbackRouteOffsetMilestone>()
    for (item in items) {
        val derived = newFallbackRouteOffsetMilestone(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeFallbackRouteOffsetMilestone(item: FallbackRouteOffsetMilestone): String {
    return classifyIdlingProfile(item.effectiveValue())
}
