package com.acme.transport.fleetops.utilization.milestone

import com.acme.transport.fleetops.utilization.exception.DefaultUtilizationPreference
import com.acme.transport.fleetops.utilization.exception.defaultUtilizationPreferenceWeight

fun summarizeDefaultUtilizationPreferencesForQuota(items: MutableList<DefaultUtilizationPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveRollingUtilizationQuotasFromPreference(items: MutableList<DefaultUtilizationPreference>): MutableList<RollingUtilizationQuota> {
    val out = mutableListOf<RollingUtilizationQuota>()
    for (item in items) {
        val derived = newRollingUtilizationQuota(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun deriveRollingUtilizationQuota(item: RollingUtilizationQuota): Double {
    return defaultUtilizationPreferenceWeight(9, true)
}
