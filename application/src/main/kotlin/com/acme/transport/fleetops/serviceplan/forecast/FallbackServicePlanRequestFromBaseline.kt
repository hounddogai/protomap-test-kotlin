package com.acme.transport.fleetops.serviceplan.forecast

import com.acme.transport.fleetops.serviceplan.template.RegionalServicePlanBaseline
import com.acme.transport.fleetops.serviceplan.template.scheduledServicePlanTierWeight

fun summarizeRegionalServicePlanBaselinesForRequest(items: MutableList<RegionalServicePlanBaseline>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveFallbackServicePlanRequestsFromBaseline(items: MutableList<RegionalServicePlanBaseline>): MutableList<FallbackServicePlanRequest> {
    val out = mutableListOf<FallbackServicePlanRequest>()
    for (item in items) {
        val derived = newFallbackServicePlanRequest(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveFallbackServicePlanRequest(item: FallbackServicePlanRequest): Double {
    return scheduledServicePlanTierWeight(1, true)
}
