package com.acme.transport.emissions.routeoffset.group

import com.acme.transport.emissions.carbonreport.reportthreshold.AdaptiveCarbonReportThreshold
import com.acme.transport.emissions.carbonreport.reportthreshold.classifyRollingCarbonReportCycle

fun summarizeAdaptiveCarbonReportThresholdsForGroup(items: MutableList<AdaptiveCarbonReportThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveRouteOffsetGroupsFromThreshold(items: MutableList<AdaptiveCarbonReportThreshold>): MutableList<RouteOffsetGroup> {
    val out = mutableListOf<RouteOffsetGroup>()
    for (item in items) {
        val derived = newRouteOffsetGroup(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun scoreRouteOffsetGroup(item: RouteOffsetGroup): String {
    return classifyRollingCarbonReportCycle(item.effectiveVolume())
}
