package com.acme.transport.emissions.fuelburn.burnmilestone

import com.acme.transport.emissions.carbonreport.reportmapping.PrimaryCarbonReportTemplate
import com.acme.transport.emissions.carbonreport.reportmapping.globalCarbonReportAdjustmentBucket

fun summarizePrimaryCarbonReportTemplatesForEstimate(items: MutableList<PrimaryCarbonReportTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveGlobalFuelBurnEstimatesFromTemplate(items: MutableList<PrimaryCarbonReportTemplate>): MutableList<GlobalFuelBurnEstimate> {
    val out = mutableListOf<GlobalFuelBurnEstimate>()
    for (item in items) {
        val derived = newGlobalFuelBurnEstimate(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun computeGlobalFuelBurnEstimate(item: GlobalFuelBurnEstimate): String {
    return globalCarbonReportAdjustmentBucket(item.netTotal())
}
