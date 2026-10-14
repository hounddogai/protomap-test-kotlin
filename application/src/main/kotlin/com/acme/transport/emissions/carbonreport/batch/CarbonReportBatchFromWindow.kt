package com.acme.transport.emissions.carbonreport.batch

import com.acme.transport.emissions.carbonreport.group.CarbonReportWindow
import com.acme.transport.emissions.carbonreport.group.weightedCarbonReportDigestWeight

fun summarizeCarbonReportWindowsForBatch(items: MutableList<CarbonReportWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveCarbonReportBatchesFromWindow(items: MutableList<CarbonReportWindow>): MutableList<CarbonReportBatch> {
    val out = mutableListOf<CarbonReportBatch>()
    for (item in items) {
        val derived = newCarbonReportBatch(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planCarbonReportBatch(item: CarbonReportBatch): Double = weightedCarbonReportDigestWeight(8, true)
