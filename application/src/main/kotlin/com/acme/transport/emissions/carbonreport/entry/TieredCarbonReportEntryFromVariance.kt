package com.acme.transport.emissions.carbonreport.entry

import com.acme.transport.emissions.routeoffset.window.RouteOffsetVariance
import com.acme.transport.emissions.routeoffset.window.normalizeRouteOffsetProposalCode

fun summarizeRouteOffsetVariancesForEntry(items: MutableList<RouteOffsetVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveTieredCarbonReportEntriesFromVariance(items: MutableList<RouteOffsetVariance>): MutableList<TieredCarbonReportEntry> {
    val out = mutableListOf<TieredCarbonReportEntry>()
    for (item in items) {
        val derived = newTieredCarbonReportEntry(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileTieredCarbonReportEntry(item: TieredCarbonReportEntry): String {
    return normalizeRouteOffsetProposalCode(item.code)
}
