package com.acme.transport.emissions.carbonreport.entry

import com.acme.transport.emissions.emissionfactor.target.FallbackEmissionFactorRequest
import com.acme.transport.emissions.emissionfactor.target.fallbackEmissionFactorRequestWeight

fun summarizeFallbackEmissionFactorRequestsForEntry(items: MutableList<FallbackEmissionFactorRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveTieredCarbonReportEntriesFromRequest(items: MutableList<FallbackEmissionFactorRequest>): MutableList<TieredCarbonReportEntry> {
    val out = mutableListOf<TieredCarbonReportEntry>()
    for (item in items) {
        val derived = newTieredCarbonReportEntry(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun projectTieredCarbonReportEntry(item: TieredCarbonReportEntry): Double {
    return fallbackEmissionFactorRequestWeight(3, true)
}
