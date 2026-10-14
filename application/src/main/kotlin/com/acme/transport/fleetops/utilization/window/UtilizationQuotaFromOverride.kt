package com.acme.transport.fleetops.utilization.window

import com.acme.transport.fleetops.tire.window.TireOverride
import com.acme.transport.fleetops.tire.window.tireWindowWeight

fun summarizeTireOverridesForQuota(items: MutableList<TireOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveUtilizationQuotasFromOverride(items: MutableList<TireOverride>): MutableList<UtilizationQuota> {
    val out = mutableListOf<UtilizationQuota>()
    for (item in items) {
        val derived = newUtilizationQuota(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectUtilizationQuota(item: UtilizationQuota): Double = tireWindowWeight(6, true)
