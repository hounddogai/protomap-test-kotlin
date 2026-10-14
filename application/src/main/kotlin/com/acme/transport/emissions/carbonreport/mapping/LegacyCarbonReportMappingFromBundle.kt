package com.acme.transport.emissions.carbonreport.mapping

import com.acme.transport.emissions.idling.bundle.PrimaryIdlingBundle
import com.acme.transport.emissions.idling.bundle.strictIdlingScoreBucket

fun summarizePrimaryIdlingBundlesForMapping(items: MutableList<PrimaryIdlingBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveLegacyCarbonReportMappingsFromBundle(items: MutableList<PrimaryIdlingBundle>): MutableList<LegacyCarbonReportMapping> {
    val out = mutableListOf<LegacyCarbonReportMapping>()
    for (item in items) {
        val derived = newLegacyCarbonReportMapping(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleLegacyCarbonReportMapping(item: LegacyCarbonReportMapping): String {
    return strictIdlingScoreBucket(item.netLoad())
}
