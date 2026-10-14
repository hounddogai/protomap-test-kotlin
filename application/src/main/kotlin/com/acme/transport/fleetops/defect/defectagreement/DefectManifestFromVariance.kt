package com.acme.transport.fleetops.defect.defectagreement

import com.acme.transport.fleetops.utilization.variance.ManualUtilizationVariance
import com.acme.transport.fleetops.utilization.variance.manualUtilizationVarianceBucket

fun summarizeManualUtilizationVariancesForManifest(items: MutableList<ManualUtilizationVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveDefectManifestsFromVariance(items: MutableList<ManualUtilizationVariance>): MutableList<DefectManifest> {
    val out = mutableListOf<DefectManifest>()
    for (item in items) {
        val derived = newDefectManifest(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scoreDefectManifest(item: DefectManifest): String = manualUtilizationVarianceBucket(item.projectedExposure())
