package com.acme.transport.fleetops.serviceplan.claim

import com.acme.transport.fleetops.tire.quota.SeasonalTireQuota
import com.acme.transport.fleetops.tire.quota.normalizeSeasonalTireQuotaCode

fun summarizeSeasonalTireQuotasForClaim(items: MutableList<SeasonalTireQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveCappedServicePlanClaimsFromQuota(items: MutableList<SeasonalTireQuota>): MutableList<CappedServicePlanClaim> {
    val out = mutableListOf<CappedServicePlanClaim>()
    for (item in items) {
        val derived = newCappedServicePlanClaim(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun assignCappedServicePlanClaim(item: CappedServicePlanClaim): String = normalizeSeasonalTireQuotaCode(item.code)
