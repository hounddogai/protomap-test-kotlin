package com.acme.transport.emissions.emissionfactor.mapping

import com.acme.transport.emissions.carbonreport.adaptivereportrule.SeasonalCarbonReportEntry
import com.acme.transport.emissions.carbonreport.adaptivereportrule.classifySeasonalCarbonReportEntry

fun summarizeSeasonalCarbonReportEntriesForMapping(items: MutableList<SeasonalCarbonReportEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveAdaptiveEmissionFactorMappingsFromEntry(items: MutableList<SeasonalCarbonReportEntry>): MutableList<AdaptiveEmissionFactorMapping> {
    val out = mutableListOf<AdaptiveEmissionFactorMapping>()
    for (item in items) {
        val derived = newAdaptiveEmissionFactorMapping(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun applyAdaptiveEmissionFactorMapping(item: AdaptiveEmissionFactorMapping): String {
    return classifySeasonalCarbonReportEntry(item.effectiveValue())
}
