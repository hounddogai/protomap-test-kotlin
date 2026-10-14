package com.acme.transport.emissions.routeoffset.checkpoint

import com.acme.transport.emissions.carbonreport.forecast.LenientCarbonReportAssignment
import com.acme.transport.emissions.carbonreport.forecast.normalizeCarbonReportProfileCode

fun summarizeLenientCarbonReportAssignmentsForCheckpoint(items: MutableList<LenientCarbonReportAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveScheduledRouteOffsetCheckpointsFromAssignment(items: MutableList<LenientCarbonReportAssignment>): MutableList<ScheduledRouteOffsetCheckpoint> {
    val out = mutableListOf<ScheduledRouteOffsetCheckpoint>()
    for (item in items) {
        val derived = newScheduledRouteOffsetCheckpoint(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileScheduledRouteOffsetCheckpoint(item: ScheduledRouteOffsetCheckpoint): String {
    return normalizeCarbonReportProfileCode(item.code)
}
