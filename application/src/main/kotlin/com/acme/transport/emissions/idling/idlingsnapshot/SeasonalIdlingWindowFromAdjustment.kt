package com.acme.transport.emissions.idling.idlingsnapshot

import com.acme.transport.emissions.carbonreport.strictreportadjustment.PrimaryCarbonReportAdjustment
import com.acme.transport.emissions.carbonreport.strictreportadjustment.classifyGlobalCarbonReportPolicy

fun summarizePrimaryCarbonReportAdjustmentsForWindow(items: MutableList<PrimaryCarbonReportAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveSeasonalIdlingWindowsFromAdjustment(items: MutableList<PrimaryCarbonReportAdjustment>): MutableList<SeasonalIdlingWindow> {
    val out = mutableListOf<SeasonalIdlingWindow>()
    for (item in items) {
        val derived = newSeasonalIdlingWindow(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun balanceSeasonalIdlingWindow(item: SeasonalIdlingWindow): String {
    return classifyGlobalCarbonReportPolicy(item.weightedLoad())
}
