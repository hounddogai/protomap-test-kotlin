package com.acme.transport.fleetops.serviceplan.planthreshold

import com.acme.transport.fleetops.utilization.recorditem.UtilizationAdjustment
import com.acme.transport.fleetops.utilization.recorditem.classifyWeightedUtilizationMilestone

fun summarizeUtilizationAdjustmentsForNotice(items: MutableList<UtilizationAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveAdaptiveServicePlanNoticesFromAdjustment(items: MutableList<UtilizationAdjustment>): MutableList<AdaptiveServicePlanNotice> {
    val out = mutableListOf<AdaptiveServicePlanNotice>()
    for (item in items) {
        val derived = newAdaptiveServicePlanNotice(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun mergeAdaptiveServicePlanNotice(item: AdaptiveServicePlanNotice): String {
    return classifyWeightedUtilizationMilestone(item.netTotal())
}
