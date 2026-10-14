package com.acme.transport.emissions.routeoffset.segment

import com.acme.transport.emissions.emissionfactor.schedule.EmissionFactorSegment
import com.acme.transport.emissions.emissionfactor.schedule.emissionFactorScheduleWeight

fun summarizeEmissionFactorSegmentsForMilestone(items: MutableList<EmissionFactorSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveRouteOffsetMilestonesFromSegment(items: MutableList<EmissionFactorSegment>): MutableList<RouteOffsetMilestone> {
    val out = mutableListOf<RouteOffsetMilestone>()
    for (item in items) {
        val derived = newRouteOffsetMilestone(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveRouteOffsetMilestone(item: RouteOffsetMilestone): Double = emissionFactorScheduleWeight(7, true)
