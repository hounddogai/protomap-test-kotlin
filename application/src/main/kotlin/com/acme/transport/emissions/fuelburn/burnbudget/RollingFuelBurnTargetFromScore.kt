package com.acme.transport.emissions.fuelburn.burnbudget

import com.acme.transport.emissions.routeoffset.claim.AdaptiveRouteOffsetScore
import com.acme.transport.emissions.routeoffset.claim.tieredRouteOffsetClaimWeight

fun summarizeAdaptiveRouteOffsetScoresForTarget(items: MutableList<AdaptiveRouteOffsetScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveRollingFuelBurnTargetsFromScore(items: MutableList<AdaptiveRouteOffsetScore>): MutableList<RollingFuelBurnTarget> {
    val out = mutableListOf<RollingFuelBurnTarget>()
    for (item in items) {
        val derived = newRollingFuelBurnTarget(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizeRollingFuelBurnTarget(item: RollingFuelBurnTarget): Double = tieredRouteOffsetClaimWeight(1, true)
