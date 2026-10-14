package com.acme.transport.emissions.idling.idlingwindow

import com.acme.transport.emissions.carbonreport.tieredreportscore.SeasonalCarbonReportEstimate
import com.acme.transport.emissions.carbonreport.tieredreportscore.carbonReportBundleWeight

fun summarizeSeasonalCarbonReportEstimatesForWindow(items: MutableList<SeasonalCarbonReportEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveScheduledIdlingWindowsFromEstimate(items: MutableList<SeasonalCarbonReportEstimate>): MutableList<ScheduledIdlingWindow> {
    val out = mutableListOf<ScheduledIdlingWindow>()
    for (item in items) {
        val derived = newScheduledIdlingWindow(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun matchScheduledIdlingWindow(item: ScheduledIdlingWindow): Double = carbonReportBundleWeight(8, true)
