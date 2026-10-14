package com.acme.transport.network.servicearea.areatarget

import com.acme.transport.network.transittime.allocation.RollingTransitTimeQuota
import com.acme.transport.network.transittime.allocation.rollingTransitTimeQuotaWeight

fun summarizeRollingTransitTimeQuotasForAdjustment(items: MutableList<RollingTransitTimeQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveStrictServiceAreaAdjustmentsFromQuota(items: MutableList<RollingTransitTimeQuota>): MutableList<StrictServiceAreaAdjustment> {
    val out = mutableListOf<StrictServiceAreaAdjustment>()
    for (item in items) {
        val derived = newStrictServiceAreaAdjustment(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun planStrictServiceAreaAdjustment(item: StrictServiceAreaAdjustment): Double {
    return rollingTransitTimeQuotaWeight(7, true)
}
