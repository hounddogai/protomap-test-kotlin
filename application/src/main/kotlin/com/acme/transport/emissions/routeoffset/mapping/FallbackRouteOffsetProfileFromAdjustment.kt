package com.acme.transport.emissions.routeoffset.mapping

import com.acme.transport.emissions.carbonreport.adjustment.CarbonReportAdjustment
import com.acme.transport.emissions.carbonreport.adjustment.tieredCarbonReportAdjustmentWeight

fun summarizeCarbonReportAdjustmentsForProfile(items: MutableList<CarbonReportAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveFallbackRouteOffsetProfilesFromAdjustment(items: MutableList<CarbonReportAdjustment>): MutableList<FallbackRouteOffsetProfile> {
    val out = mutableListOf<FallbackRouteOffsetProfile>()
    for (item in items) {
        val derived = newFallbackRouteOffsetProfile(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun scoreFallbackRouteOffsetProfile(item: FallbackRouteOffsetProfile): Double {
    return tieredCarbonReportAdjustmentWeight(4, true)
}
