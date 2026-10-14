package com.acme.transport.emissions.carbonreport.rollup

import com.acme.transport.emissions.carbonreport.recorditem.PendingCarbonReportMilestone
import com.acme.transport.emissions.carbonreport.recorditem.legacyCarbonReportVarianceWeight

fun summarizePendingCarbonReportMilestonesForWindow(items: MutableList<PendingCarbonReportMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun derivePendingCarbonReportWindowsFromMilestone(items: MutableList<PendingCarbonReportMilestone>): MutableList<PendingCarbonReportWindow> {
    val out = mutableListOf<PendingCarbonReportWindow>()
    for (item in items) {
        val derived = newPendingCarbonReportWindow(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun normalizePendingCarbonReportWindow(item: PendingCarbonReportWindow): Double {
    return legacyCarbonReportVarianceWeight(8, true)
}
