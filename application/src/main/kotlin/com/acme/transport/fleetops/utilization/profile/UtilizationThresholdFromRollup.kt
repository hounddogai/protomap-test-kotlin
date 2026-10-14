package com.acme.transport.fleetops.utilization.profile

import com.acme.transport.fleetops.serviceplan.rollup.ServicePlanRollup
import com.acme.transport.fleetops.serviceplan.rollup.classifyServicePlanDraft

fun summarizeServicePlanRollupsForThreshold(items: MutableList<ServicePlanRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveUtilizationThresholdsFromRollup(items: MutableList<ServicePlanRollup>): MutableList<UtilizationThreshold> {
    val out = mutableListOf<UtilizationThreshold>()
    for (item in items) {
        val derived = newUtilizationThreshold(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun classifyUtilizationThreshold(item: UtilizationThreshold): String {
    return classifyServicePlanDraft(item.weightedExposure())
}
