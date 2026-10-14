package com.acme.transport.fleetops.utilization.milestone

import com.acme.transport.fleetops.tire.target.TireThreshold
import com.acme.transport.fleetops.tire.target.normalizeTireThresholdCode

fun summarizeTireThresholdsForQuota(items: MutableList<TireThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveRollingUtilizationQuotasFromThreshold(items: MutableList<TireThreshold>): MutableList<RollingUtilizationQuota> {
    val out = mutableListOf<RollingUtilizationQuota>()
    for (item in items) {
        val derived = newRollingUtilizationQuota(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeRollingUtilizationQuota(item: RollingUtilizationQuota): String {
    return normalizeTireThresholdCode(item.code)
}
