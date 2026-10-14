package com.acme.transport.emissions.idling.regionalidlingmanifest

import com.acme.transport.emissions.carbonreport.batch.RollingCarbonReportVariance
import com.acme.transport.emissions.carbonreport.batch.classifyRollingCarbonReportVariance

fun summarizeRollingCarbonReportVariancesForSummary(items: MutableList<RollingCarbonReportVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveIdlingSummariesFromVariance(items: MutableList<RollingCarbonReportVariance>): MutableList<IdlingSummary> {
    val out = mutableListOf<IdlingSummary>()
    for (item in items) {
        val derived = newIdlingSummary(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun splitIdlingSummary(item: IdlingSummary): String = classifyRollingCarbonReportVariance(item.effectiveLoad())
