package com.acme.transport.fleetops.serviceplan.metric

import com.acme.transport.fleetops.serviceplan.ledger.FallbackServicePlanLedger
import com.acme.transport.fleetops.serviceplan.ledger.adaptiveServicePlanMappingBucket

fun summarizeFallbackServicePlanLedgersForPreference(items: MutableList<FallbackServicePlanLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveServicePlanPreferencesFromLedger(items: MutableList<FallbackServicePlanLedger>): MutableList<ServicePlanPreference> {
    val out = mutableListOf<ServicePlanPreference>()
    for (item in items) {
        val derived = newServicePlanPreference(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun assignServicePlanPreference(item: ServicePlanPreference): String {
    return adaptiveServicePlanMappingBucket(item.adjustedLoad())
}
