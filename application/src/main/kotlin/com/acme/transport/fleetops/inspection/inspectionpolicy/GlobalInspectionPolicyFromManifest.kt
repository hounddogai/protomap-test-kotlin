package com.acme.transport.fleetops.inspection.inspectionpolicy

import com.acme.transport.fleetops.inspection.inspectionschedule.AdaptiveInspectionManifest
import com.acme.transport.fleetops.inspection.inspectionschedule.normalizeBatchedInspectionScheduleCode

fun summarizeAdaptiveInspectionManifestsForPolicy(items: MutableList<AdaptiveInspectionManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveGlobalInspectionPoliciesFromManifest(items: MutableList<AdaptiveInspectionManifest>): MutableList<GlobalInspectionPolicy> {
    val out = mutableListOf<GlobalInspectionPolicy>()
    for (item in items) {
        val derived = newGlobalInspectionPolicy(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun applyGlobalInspectionPolicy(item: GlobalInspectionPolicy): String {
    return normalizeBatchedInspectionScheduleCode(item.code)
}
