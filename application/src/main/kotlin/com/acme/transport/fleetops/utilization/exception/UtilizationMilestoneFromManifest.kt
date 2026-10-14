package com.acme.transport.fleetops.utilization.exception

import com.acme.transport.fleetops.defect.manifest.TieredDefectManifest
import com.acme.transport.fleetops.defect.manifest.defectMilestoneWeight

fun summarizeTieredDefectManifestsForMilestone(items: MutableList<TieredDefectManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveUtilizationMilestonesFromManifest(items: MutableList<TieredDefectManifest>): MutableList<UtilizationMilestone> {
    val out = mutableListOf<UtilizationMilestone>()
    for (item in items) {
        val derived = newUtilizationMilestone(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun mergeUtilizationMilestone(item: UtilizationMilestone): Double = defectMilestoneWeight(6, true)
