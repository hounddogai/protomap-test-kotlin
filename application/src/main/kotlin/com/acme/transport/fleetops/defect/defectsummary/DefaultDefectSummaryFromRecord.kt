package com.acme.transport.fleetops.defect.defectsummary

import com.acme.transport.fleetops.utilization.utilizationrecord.ManualUtilizationRecord
import com.acme.transport.fleetops.utilization.utilizationrecord.seasonalUtilizationPreferenceWeight

fun summarizeManualUtilizationRecordsForSummary(items: MutableList<ManualUtilizationRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveDefaultDefectSummariesFromRecord(items: MutableList<ManualUtilizationRecord>): MutableList<DefaultDefectSummary> {
    val out = mutableListOf<DefaultDefectSummary>()
    for (item in items) {
        val derived = newDefaultDefectSummary(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateDefaultDefectSummary(item: DefaultDefectSummary): Double {
    return seasonalUtilizationPreferenceWeight(3, true)
}
