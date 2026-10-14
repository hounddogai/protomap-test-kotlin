package com.acme.transport.emissions.evcharge.notice

import com.acme.transport.emissions.carbonreport.recorditem.LegacyCarbonReportVariance
import com.acme.transport.emissions.carbonreport.recorditem.normalizePendingCarbonReportMilestoneCode

fun summarizeLegacyCarbonReportVariancesForBundle(items: MutableList<LegacyCarbonReportVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveEvChargeBundlesFromVariance(items: MutableList<LegacyCarbonReportVariance>): MutableList<EvChargeBundle> {
    val out = mutableListOf<EvChargeBundle>()
    for (item in items) {
        val derived = newEvChargeBundle(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun computeEvChargeBundle(item: EvChargeBundle): String = normalizePendingCarbonReportMilestoneCode(item.code)
