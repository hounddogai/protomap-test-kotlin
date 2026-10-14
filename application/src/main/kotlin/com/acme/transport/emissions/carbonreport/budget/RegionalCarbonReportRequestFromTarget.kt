package com.acme.transport.emissions.carbonreport.budget

import com.acme.transport.emissions.routeoffset.metric.WeightedRouteOffsetTarget
import com.acme.transport.emissions.routeoffset.metric.normalizeRouteOffsetClaimCode

fun summarizeWeightedRouteOffsetTargetsForRequest(items: MutableList<WeightedRouteOffsetTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveRegionalCarbonReportRequestsFromTarget(items: MutableList<WeightedRouteOffsetTarget>): MutableList<RegionalCarbonReportRequest> {
    val out = mutableListOf<RegionalCarbonReportRequest>()
    for (item in items) {
        val derived = newRegionalCarbonReportRequest(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun balanceRegionalCarbonReportRequest(item: RegionalCarbonReportRequest): String {
    return normalizeRouteOffsetClaimCode(item.code)
}
