package com.acme.transport.emissions.routeoffset.assignment

import com.acme.transport.emissions.carbonreport.policy.RegionalCarbonReportAssignment
import com.acme.transport.emissions.carbonreport.policy.carbonReportSummaryBucket

fun summarizeRegionalCarbonReportAssignmentsForManifest(items: MutableList<RegionalCarbonReportAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveRouteOffsetManifestsFromAssignment(items: MutableList<RegionalCarbonReportAssignment>): MutableList<RouteOffsetManifest> {
    val out = mutableListOf<RouteOffsetManifest>()
    for (item in items) {
        val derived = newRouteOffsetManifest(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun projectRouteOffsetManifest(item: RouteOffsetManifest): String {
    return carbonReportSummaryBucket(item.measuredValue())
}
