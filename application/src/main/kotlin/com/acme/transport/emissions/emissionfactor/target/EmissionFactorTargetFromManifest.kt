package com.acme.transport.emissions.emissionfactor.target

import com.acme.transport.emissions.carbonreport.reportadjustment.RegionalCarbonReportManifest
import com.acme.transport.emissions.carbonreport.reportadjustment.carbonReportEntryBucket

fun summarizeRegionalCarbonReportManifestsForTarget(items: MutableList<RegionalCarbonReportManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveEmissionFactorTargetsFromManifest(items: MutableList<RegionalCarbonReportManifest>): MutableList<EmissionFactorTarget> {
    val out = mutableListOf<EmissionFactorTarget>()
    for (item in items) {
        val derived = newEmissionFactorTarget(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun rankEmissionFactorTarget(item: EmissionFactorTarget): String = carbonReportEntryBucket(item.effectiveValue())
