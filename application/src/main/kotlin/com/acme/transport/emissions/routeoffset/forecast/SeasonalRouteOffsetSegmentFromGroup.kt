package com.acme.transport.emissions.routeoffset.forecast

import com.acme.transport.emissions.carbonreport.regionalreportrule.CappedCarbonReportGroup
import com.acme.transport.emissions.carbonreport.regionalreportrule.classifyBatchedCarbonReportAudit

fun summarizeCappedCarbonReportGroupsForSegment(items: MutableList<CappedCarbonReportGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveSeasonalRouteOffsetSegmentsFromGroup(items: MutableList<CappedCarbonReportGroup>): MutableList<SeasonalRouteOffsetSegment> {
    val out = mutableListOf<SeasonalRouteOffsetSegment>()
    for (item in items) {
        val derived = newSeasonalRouteOffsetSegment(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun planSeasonalRouteOffsetSegment(item: SeasonalRouteOffsetSegment): String {
    return classifyBatchedCarbonReportAudit(item.effectiveExposure())
}
