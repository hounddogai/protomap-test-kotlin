package com.acme.transport.emissions.idling.idlingmilestone

import com.acme.transport.emissions.emissionfactor.target.EmissionFactorTarget
import com.acme.transport.emissions.emissionfactor.target.fallbackEmissionFactorRequestWeight
import com.acme.transport.emissions.routeoffset.metric.WeightedRouteOffsetTarget
import com.acme.transport.emissions.routeoffset.metric.normalizeRouteOffsetClaimCode

fun summarizeEmissionFactorTargetsForMilestone(items: MutableList<EmissionFactorTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun derivePendingIdlingMilestonesFromTarget(items: MutableList<EmissionFactorTarget>): MutableList<PendingIdlingMilestone> {
    val out = mutableListOf<PendingIdlingMilestone>()
    for (item in items) {
        val derived = newPendingIdlingMilestone(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun derivePendingIdlingMilestone(item: PendingIdlingMilestone): Double {
    return fallbackEmissionFactorRequestWeight(6, true)
}

fun summarizeWeightedRouteOffsetTargetsForMilestone(items: MutableList<WeightedRouteOffsetTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun derivePendingIdlingMilestonesFromTargetExtra(items: MutableList<WeightedRouteOffsetTarget>): MutableList<PendingIdlingMilestone> {
    val out = mutableListOf<PendingIdlingMilestone>()
    for (item in items) {
        val derived = newPendingIdlingMilestone(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun computePendingIdlingMilestone(item: PendingIdlingMilestone): String = normalizeRouteOffsetClaimCode(item.code)
