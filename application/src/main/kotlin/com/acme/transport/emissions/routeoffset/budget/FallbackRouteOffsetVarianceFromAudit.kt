package com.acme.transport.emissions.routeoffset.budget

import com.acme.transport.emissions.fuelburn.pendingburnthreshold.RegionalFuelBurnAudit
import com.acme.transport.emissions.fuelburn.pendingburnthreshold.scheduledFuelBurnThresholdWeight

fun summarizeRegionalFuelBurnAuditsForVariance(items: MutableList<RegionalFuelBurnAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveFallbackRouteOffsetVariancesFromAudit(items: MutableList<RegionalFuelBurnAudit>): MutableList<FallbackRouteOffsetVariance> {
    val out = mutableListOf<FallbackRouteOffsetVariance>()
    for (item in items) {
        val derived = newFallbackRouteOffsetVariance(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileFallbackRouteOffsetVariance(item: FallbackRouteOffsetVariance): Double {
    return scheduledFuelBurnThresholdWeight(1, true)
}
