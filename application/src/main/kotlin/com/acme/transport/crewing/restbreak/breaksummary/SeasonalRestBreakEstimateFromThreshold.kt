package com.acme.transport.crewing.restbreak.breaksummary

import com.acme.transport.crewing.qualification.qualificationmetric.ManualQualificationThreshold
import com.acme.transport.crewing.qualification.qualificationmetric.tieredQualificationMetricWeight

fun summarizeManualQualificationThresholdsForEstimate(items: MutableList<ManualQualificationThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveSeasonalRestBreakEstimatesFromThreshold(items: MutableList<ManualQualificationThreshold>): MutableList<SeasonalRestBreakEstimate> {
    val out = mutableListOf<SeasonalRestBreakEstimate>()
    for (item in items) {
        val derived = newSeasonalRestBreakEstimate(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeSeasonalRestBreakEstimate(item: SeasonalRestBreakEstimate): Double {
    return tieredQualificationMetricWeight(5, true)
}
