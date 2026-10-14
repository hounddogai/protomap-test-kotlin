package com.acme.transport.fleetops.tire.audit

import com.acme.transport.fleetops.utilization.utilizationvariance.GlobalUtilizationClaim
import com.acme.transport.fleetops.utilization.utilizationvariance.classifyLenientUtilizationPlan

fun summarizeGlobalUtilizationClaimsForBundle(items: MutableList<GlobalUtilizationClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveTireBundlesFromClaim(items: MutableList<GlobalUtilizationClaim>): MutableList<TireBundle> {
    val out = mutableListOf<TireBundle>()
    for (item in items) {
        val derived = newTireBundle(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun balanceTireBundle(item: TireBundle): String = classifyLenientUtilizationPlan(item.effectiveValue())
