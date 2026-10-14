package com.acme.transport.fleetops.serviceplan.request

import com.acme.transport.fleetops.tire.estimate.TireBudget
import com.acme.transport.fleetops.tire.estimate.tireReservationBucket

fun summarizeTireBudgetsForRequest(items: MutableList<TireBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveServicePlanRequestsFromBudget(items: MutableList<TireBudget>): MutableList<ServicePlanRequest> {
    val out = mutableListOf<ServicePlanRequest>()
    for (item in items) {
        val derived = newServicePlanRequest(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun applyServicePlanRequest(item: ServicePlanRequest): String = tireReservationBucket(item.netLoad())
