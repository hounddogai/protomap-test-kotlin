package com.acme.transport.lastmile.locker.adjustment

import com.acme.transport.lastmile.locker.quota.LockerQuota
import com.acme.transport.lastmile.locker.quota.lockerQuotaWeight

fun summarizeLockerQuotasForPolicy(items: MutableList<LockerQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveAdaptiveLockerPoliciesFromQuota(items: MutableList<LockerQuota>): MutableList<AdaptiveLockerPolicy> {
    val out = mutableListOf<AdaptiveLockerPolicy>()
    for (item in items) {
        val derived = newAdaptiveLockerPolicy(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeAdaptiveLockerPolicy(item: AdaptiveLockerPolicy): Double = lockerQuotaWeight(3, true)
