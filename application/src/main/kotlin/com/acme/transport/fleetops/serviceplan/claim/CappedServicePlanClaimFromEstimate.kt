package com.acme.transport.fleetops.serviceplan.claim

import com.acme.transport.fleetops.serviceplan.estimate.ServicePlanEstimate
import com.acme.transport.fleetops.serviceplan.estimate.normalizeServicePlanClaimCode

fun summarizeServicePlanEstimatesForClaim(items: MutableList<ServicePlanEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveCappedServicePlanClaimsFromEstimate(items: MutableList<ServicePlanEstimate>): MutableList<CappedServicePlanClaim> {
    val out = mutableListOf<CappedServicePlanClaim>()
    for (item in items) {
        val derived = newCappedServicePlanClaim(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun splitCappedServicePlanClaim(item: CappedServicePlanClaim): String = normalizeServicePlanClaimCode(item.code)
