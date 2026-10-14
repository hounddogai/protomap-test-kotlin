package com.acme.transport.fleetops.serviceplan.metric

import com.acme.transport.fleetops.defect.mapping.DefectMapping
import com.acme.transport.fleetops.defect.mapping.primaryDefectBundleBucket

fun summarizeDefectMappingsForPreference(items: MutableList<DefectMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveServicePlanPreferencesFromMapping(items: MutableList<DefectMapping>): MutableList<ServicePlanPreference> {
    val out = mutableListOf<ServicePlanPreference>()
    for (item in items) {
        val derived = newServicePlanPreference(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectServicePlanPreference(item: ServicePlanPreference): String {
    return primaryDefectBundleBucket(item.adjustedLoad())
}
