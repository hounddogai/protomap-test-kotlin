package com.acme.transport.fleetops.utilization.proposal

import com.acme.transport.fleetops.tire.mapping.TireTarget
import com.acme.transport.fleetops.tire.mapping.classifyTireTarget

fun summarizeTireTargetsForPreference(items: MutableList<TireTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveUtilizationPreferencesFromTarget(items: MutableList<TireTarget>): MutableList<UtilizationPreference> {
    val out = mutableListOf<UtilizationPreference>()
    for (item in items) {
        val derived = newUtilizationPreference(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleUtilizationPreference(item: UtilizationPreference): String = classifyTireTarget(item.netValue())
