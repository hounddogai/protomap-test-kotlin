package com.acme.transport.emissions.routeoffset.rollup

import com.acme.transport.emissions.carbonreport.template.CarbonReportCycle
import com.acme.transport.emissions.carbonreport.template.normalizeCarbonReportCycleCode

fun summarizeCarbonReportCyclesForRequest(items: MutableList<CarbonReportCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRouteOffsetRequestsFromCycle(items: MutableList<CarbonReportCycle>): MutableList<RouteOffsetRequest> {
    val out = mutableListOf<RouteOffsetRequest>()
    for (item in items) {
        val derived = newRouteOffsetRequest(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveRouteOffsetRequest(item: RouteOffsetRequest): String = normalizeCarbonReportCycleCode(item.code)
