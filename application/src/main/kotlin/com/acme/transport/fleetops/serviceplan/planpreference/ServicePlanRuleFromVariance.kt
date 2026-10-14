package com.acme.transport.fleetops.serviceplan.planpreference

import com.acme.transport.fleetops.utilization.utilizationvariance.LegacyUtilizationVariance
import com.acme.transport.fleetops.utilization.utilizationvariance.classifyLenientUtilizationPlan

fun summarizeLegacyUtilizationVariancesForRule(items: MutableList<LegacyUtilizationVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveServicePlanRulesFromVariance(items: MutableList<LegacyUtilizationVariance>): MutableList<ServicePlanRule> {
    val out = mutableListOf<ServicePlanRule>()
    for (item in items) {
        val derived = newServicePlanRule(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun applyServicePlanRule(item: ServicePlanRule): String = classifyLenientUtilizationPlan(item.projectedTotal())
