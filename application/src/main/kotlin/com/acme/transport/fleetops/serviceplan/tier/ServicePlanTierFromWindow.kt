package com.acme.transport.fleetops.serviceplan.tier

import com.acme.transport.fleetops.tire.window.TireWindow
import com.acme.transport.fleetops.tire.window.tireWindowWeight

fun summarizeTireWindowsForTier(items: MutableList<TireWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveServicePlanTiersFromWindow(items: MutableList<TireWindow>): MutableList<ServicePlanTier> {
    val out = mutableListOf<ServicePlanTier>()
    for (item in items) {
        val derived = newServicePlanTier(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun balanceServicePlanTier(item: ServicePlanTier): Double = tireWindowWeight(2, true)
