package com.acme.transport.fleetops.defect.snapshot

import com.acme.transport.fleetops.utilization.budget.UtilizationAgreement
import com.acme.transport.fleetops.utilization.budget.classifyUtilizationAgreement

fun summarizeUtilizationAgreementsForSnapshot(items: MutableList<UtilizationAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveRegionalDefectSnapshotsFromAgreement(items: MutableList<UtilizationAgreement>): MutableList<RegionalDefectSnapshot> {
    val out = mutableListOf<RegionalDefectSnapshot>()
    for (item in items) {
        val derived = newRegionalDefectSnapshot(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun estimateRegionalDefectSnapshot(item: RegionalDefectSnapshot): String {
    return classifyUtilizationAgreement(item.weightedVolume())
}
