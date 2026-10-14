package com.acme.transport.emissions.emissionfactor.request

import com.acme.transport.emissions.routeoffset.mapping.RegionalRouteOffsetMapping
import com.acme.transport.emissions.routeoffset.mapping.classifyFallbackRouteOffsetProfile

fun summarizeRegionalRouteOffsetMappingsForMapping(items: MutableList<RegionalRouteOffsetMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveStrictEmissionFactorMappingsFromMapping(items: MutableList<RegionalRouteOffsetMapping>): MutableList<StrictEmissionFactorMapping> {
    val out = mutableListOf<StrictEmissionFactorMapping>()
    for (item in items) {
        val derived = newStrictEmissionFactorMapping(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun applyStrictEmissionFactorMapping(item: StrictEmissionFactorMapping): String {
    return classifyFallbackRouteOffsetProfile(item.effectiveTotal())
}
