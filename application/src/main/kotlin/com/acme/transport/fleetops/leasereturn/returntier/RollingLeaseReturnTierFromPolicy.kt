package com.acme.transport.fleetops.leasereturn.returntier

import com.acme.transport.fleetops.utilization.audit.SeasonalUtilizationPolicy
import com.acme.transport.fleetops.utilization.audit.seasonalUtilizationPolicyWeight

fun summarizeSeasonalUtilizationPoliciesForTier(items: MutableList<SeasonalUtilizationPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveRollingLeaseReturnTiersFromPolicy(items: MutableList<SeasonalUtilizationPolicy>): MutableList<RollingLeaseReturnTier> {
    val out = mutableListOf<RollingLeaseReturnTier>()
    for (item in items) {
        val derived = newRollingLeaseReturnTier(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun assignRollingLeaseReturnTier(item: RollingLeaseReturnTier): Double = seasonalUtilizationPolicyWeight(1, true)
