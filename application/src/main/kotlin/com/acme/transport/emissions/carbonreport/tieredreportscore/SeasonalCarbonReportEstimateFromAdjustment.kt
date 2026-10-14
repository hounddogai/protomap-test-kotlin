package com.acme.transport.emissions.carbonreport.tieredreportscore

import com.acme.transport.emissions.carbonreport.strictreportadjustment.PrimaryCarbonReportAdjustment
import com.acme.transport.emissions.carbonreport.strictreportadjustment.normalizePrimaryCarbonReportAdjustmentCode

fun summarizePrimaryCarbonReportAdjustmentsForEstimate(items: MutableList<PrimaryCarbonReportAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveSeasonalCarbonReportEstimatesFromAdjustment(items: MutableList<PrimaryCarbonReportAdjustment>): MutableList<SeasonalCarbonReportEstimate> {
    val out = mutableListOf<SeasonalCarbonReportEstimate>()
    for (item in items) {
        val derived = newSeasonalCarbonReportEstimate(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun mergeSeasonalCarbonReportEstimate(item: SeasonalCarbonReportEstimate): String {
    return normalizePrimaryCarbonReportAdjustmentCode(item.code)
}
