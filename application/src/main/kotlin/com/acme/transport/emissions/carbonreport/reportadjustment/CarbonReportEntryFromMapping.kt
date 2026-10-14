package com.acme.transport.emissions.carbonreport.reportadjustment

import com.acme.transport.emissions.routeoffset.offsetmapping.FallbackRouteOffsetMapping
import com.acme.transport.emissions.routeoffset.offsetmapping.normalizeFallbackRouteOffsetMappingCode

fun summarizeFallbackRouteOffsetMappingsForEntry(items: MutableList<FallbackRouteOffsetMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveCarbonReportEntriesFromMapping(items: MutableList<FallbackRouteOffsetMapping>): MutableList<CarbonReportEntry> {
    val out = mutableListOf<CarbonReportEntry>()
    for (item in items) {
        val derived = newCarbonReportEntry(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankCarbonReportEntry(item: CarbonReportEntry): String = normalizeFallbackRouteOffsetMappingCode(item.code)
