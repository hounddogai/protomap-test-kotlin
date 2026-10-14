package com.acme.transport.fleetops.inspection.schedule

import com.acme.transport.fleetops.inspection.policy.InspectionAgreement
import com.acme.transport.fleetops.inspection.policy.inspectionBundleBucket

fun summarizeInspectionAgreementsForSchedule(items: MutableList<InspectionAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveAdaptiveInspectionSchedulesFromAgreement(items: MutableList<InspectionAgreement>): MutableList<AdaptiveInspectionSchedule> {
    val out = mutableListOf<AdaptiveInspectionSchedule>()
    for (item in items) {
        val derived = newAdaptiveInspectionSchedule(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyAdaptiveInspectionSchedule(item: AdaptiveInspectionSchedule): String {
    return inspectionBundleBucket(item.projectedVolume())
}
