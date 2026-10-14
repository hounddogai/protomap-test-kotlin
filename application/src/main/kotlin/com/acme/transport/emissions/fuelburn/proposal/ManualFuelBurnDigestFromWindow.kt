package com.acme.transport.emissions.fuelburn.proposal

import com.acme.transport.emissions.carbonreport.group.CarbonReportWindow
import com.acme.transport.emissions.carbonreport.group.regionalCarbonReportGroupBucket

fun summarizeCarbonReportWindowsForDigest(items: MutableList<CarbonReportWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveManualFuelBurnDigestsFromWindow(items: MutableList<CarbonReportWindow>): MutableList<ManualFuelBurnDigest> {
    val out = mutableListOf<ManualFuelBurnDigest>()
    for (item in items) {
        val derived = newManualFuelBurnDigest(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileManualFuelBurnDigest(item: ManualFuelBurnDigest): String {
    return regionalCarbonReportGroupBucket(item.netValue())
}
