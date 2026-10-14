package com.acme.transport.emissions.idling.score

import com.acme.transport.emissions.carbonreport.threshold.CarbonReportThreshold
import com.acme.transport.emissions.carbonreport.threshold.adaptiveCarbonReportGroupWeight

fun summarizeCarbonReportThresholdsForScore(items: MutableList<CarbonReportThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveManualIdlingScoresFromThreshold(items: MutableList<CarbonReportThreshold>): MutableList<ManualIdlingScore> {
    val out = mutableListOf<ManualIdlingScore>()
    for (item in items) {
        val derived = newManualIdlingScore(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun rankManualIdlingScore(item: ManualIdlingScore): Double = adaptiveCarbonReportGroupWeight(4, true)
