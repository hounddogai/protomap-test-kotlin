package com.acme.transport.fleetops.serviceplan.summary

import com.acme.transport.fleetops.leasereturn.target.LeaseReturnTarget
import com.acme.transport.fleetops.leasereturn.target.classifyLeaseReturnPlan

fun summarizeLeaseReturnTargetsForForecast(items: MutableList<LeaseReturnTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveServicePlanForecastsFromTarget(items: MutableList<LeaseReturnTarget>): MutableList<ServicePlanForecast> {
    val out = mutableListOf<ServicePlanForecast>()
    for (item in items) {
        val derived = newServicePlanForecast(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateServicePlanForecast(item: ServicePlanForecast): String {
    return classifyLeaseReturnPlan(item.effectiveTotal())
}
