package com.acme.transport.fleetops.utilization.utilizationplan

import com.acme.transport.fleetops.serviceplan.planthreshold.AdaptiveServicePlanNotice
import com.acme.transport.fleetops.serviceplan.planthreshold.classifyServicePlanAdjustment

fun summarizeAdaptiveServicePlanNoticesForPolicy(items: MutableList<AdaptiveServicePlanNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveAdaptiveUtilizationPoliciesFromNotice(items: MutableList<AdaptiveServicePlanNotice>): MutableList<AdaptiveUtilizationPolicy> {
    val out = mutableListOf<AdaptiveUtilizationPolicy>()
    for (item in items) {
        val derived = newAdaptiveUtilizationPolicy(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeAdaptiveUtilizationPolicy(item: AdaptiveUtilizationPolicy): String {
    return classifyServicePlanAdjustment(item.netValue())
}
