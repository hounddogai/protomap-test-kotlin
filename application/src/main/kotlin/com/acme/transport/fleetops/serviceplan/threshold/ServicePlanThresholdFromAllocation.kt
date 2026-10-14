package com.acme.transport.fleetops.serviceplan.threshold

import com.acme.transport.fleetops.defect.cycle.RegionalDefectAllocation
import com.acme.transport.fleetops.defect.cycle.classifyLegacyDefectCycle

fun summarizeRegionalDefectAllocationsForThreshold(items: MutableList<RegionalDefectAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveServicePlanThresholdsFromAllocation(items: MutableList<RegionalDefectAllocation>): MutableList<ServicePlanThreshold> {
    val out = mutableListOf<ServicePlanThreshold>()
    for (item in items) {
        val derived = newServicePlanThreshold(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun matchServicePlanThreshold(item: ServicePlanThreshold): String {
    return classifyLegacyDefectCycle(item.projectedValue())
}
