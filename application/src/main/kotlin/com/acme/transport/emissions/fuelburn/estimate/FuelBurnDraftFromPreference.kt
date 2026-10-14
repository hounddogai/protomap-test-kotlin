package com.acme.transport.emissions.fuelburn.estimate

import com.acme.transport.emissions.carbonreport.allocation.FallbackCarbonReportPreference
import com.acme.transport.emissions.carbonreport.allocation.carbonReportManifestBucket

fun summarizeFallbackCarbonReportPreferencesForDraft(items: MutableList<FallbackCarbonReportPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveFuelBurnDraftsFromPreference(items: MutableList<FallbackCarbonReportPreference>): MutableList<FuelBurnDraft> {
    val out = mutableListOf<FuelBurnDraft>()
    for (item in items) {
        val derived = newFuelBurnDraft(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun projectFuelBurnDraft(item: FuelBurnDraft): String = carbonReportManifestBucket(item.netValue())
