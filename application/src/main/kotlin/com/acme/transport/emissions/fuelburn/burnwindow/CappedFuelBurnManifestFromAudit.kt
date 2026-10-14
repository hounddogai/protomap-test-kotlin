package com.acme.transport.emissions.fuelburn.burnwindow

import com.acme.transport.emissions.carbonreport.regionalreportrule.BatchedCarbonReportAudit
import com.acme.transport.emissions.carbonreport.regionalreportrule.normalizeLegacyCarbonReportRuleCode

fun summarizeBatchedCarbonReportAuditsForManifest(items: MutableList<BatchedCarbonReportAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveCappedFuelBurnManifestsFromAudit(items: MutableList<BatchedCarbonReportAudit>): MutableList<CappedFuelBurnManifest> {
    val out = mutableListOf<CappedFuelBurnManifest>()
    for (item in items) {
        val derived = newCappedFuelBurnManifest(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizeCappedFuelBurnManifest(item: CappedFuelBurnManifest): String {
    return normalizeLegacyCarbonReportRuleCode(item.code)
}
