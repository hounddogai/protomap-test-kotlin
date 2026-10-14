package com.acme.transport.emissions.carbonreport.threshold

import com.acme.transport.emissions.routeoffset.ledger.DefaultRouteOffsetLedger
import com.acme.transport.emissions.routeoffset.ledger.normalizeGlobalRouteOffsetPlanCode

fun summarizeDefaultRouteOffsetLedgersForGroup(items: MutableList<DefaultRouteOffsetLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveAdaptiveCarbonReportGroupsFromLedger(items: MutableList<DefaultRouteOffsetLedger>): MutableList<AdaptiveCarbonReportGroup> {
    val out = mutableListOf<AdaptiveCarbonReportGroup>()
    for (item in items) {
        val derived = newAdaptiveCarbonReportGroup(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun applyAdaptiveCarbonReportGroup(item: AdaptiveCarbonReportGroup): String {
    return normalizeGlobalRouteOffsetPlanCode(item.code)
}
