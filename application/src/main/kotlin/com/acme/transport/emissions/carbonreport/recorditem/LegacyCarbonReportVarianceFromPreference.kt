package com.acme.transport.emissions.carbonreport.recorditem

import com.acme.transport.emissions.emissionfactor.template.LegacyEmissionFactorPreference
import com.acme.transport.emissions.emissionfactor.template.classifyGlobalEmissionFactorSlot

fun summarizeLegacyEmissionFactorPreferencesForVariance(items: MutableList<LegacyEmissionFactorPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveLegacyCarbonReportVariancesFromPreference(items: MutableList<LegacyEmissionFactorPreference>): MutableList<LegacyCarbonReportVariance> {
    val out = mutableListOf<LegacyCarbonReportVariance>()
    for (item in items) {
        val derived = newLegacyCarbonReportVariance(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun mergeLegacyCarbonReportVariance(item: LegacyCarbonReportVariance): String {
    return classifyGlobalEmissionFactorSlot(item.projectedVolume())
}
