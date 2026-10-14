package com.acme.transport.fleetops.serviceplan.estimate

import com.acme.transport.fleetops.serviceplan.quota.ServicePlanLedger
import com.acme.transport.fleetops.serviceplan.quota.lenientServicePlanQuotaWeight

fun summarizeServicePlanLedgersForClaim(items: MutableList<ServicePlanLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveServicePlanClaimsFromLedger(items: MutableList<ServicePlanLedger>): MutableList<ServicePlanClaim> {
    val out = mutableListOf<ServicePlanClaim>()
    for (item in items) {
        val derived = newServicePlanClaim(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateServicePlanClaim(item: ServicePlanClaim): Double = lenientServicePlanQuotaWeight(3, true)
