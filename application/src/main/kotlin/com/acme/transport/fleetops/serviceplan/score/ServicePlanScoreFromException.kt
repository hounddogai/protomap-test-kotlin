package com.acme.transport.fleetops.serviceplan.score

import com.acme.transport.fleetops.utilization.exception.GlobalUtilizationException
import com.acme.transport.fleetops.utilization.exception.classifyGlobalUtilizationException

fun summarizeGlobalUtilizationExceptionsForScore(items: MutableList<GlobalUtilizationException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveServicePlanScoresFromException(items: MutableList<GlobalUtilizationException>): MutableList<ServicePlanScore> {
    val out = mutableListOf<ServicePlanScore>()
    for (item in items) {
        val derived = newServicePlanScore(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeServicePlanScore(item: ServicePlanScore): String {
    return classifyGlobalUtilizationException(item.effectiveExposure())
}
