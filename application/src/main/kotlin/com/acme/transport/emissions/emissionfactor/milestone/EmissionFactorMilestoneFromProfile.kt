package com.acme.transport.emissions.emissionfactor.milestone

import com.acme.transport.emissions.emissionfactor.profile.EmissionFactorProfile
import com.acme.transport.emissions.emissionfactor.profile.emissionFactorProfileWeight
import com.acme.transport.emissions.routeoffset.profile.AdaptiveRouteOffsetProfile
import com.acme.transport.emissions.routeoffset.profile.routeOffsetReservationBucket

fun summarizeAdaptiveRouteOffsetProfilesForMilestone(items: MutableList<AdaptiveRouteOffsetProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveEmissionFactorMilestonesFromProfile(items: MutableList<AdaptiveRouteOffsetProfile>): MutableList<EmissionFactorMilestone> {
    val out = mutableListOf<EmissionFactorMilestone>()
    for (item in items) {
        val derived = newEmissionFactorMilestone(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun computeEmissionFactorMilestone(item: EmissionFactorMilestone): String {
    return routeOffsetReservationBucket(item.weightedLoad())
}

fun summarizeEmissionFactorProfilesForMilestone(items: MutableList<EmissionFactorProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveEmissionFactorMilestonesFromProfileExtra(items: MutableList<EmissionFactorProfile>): MutableList<EmissionFactorMilestone> {
    val out = mutableListOf<EmissionFactorMilestone>()
    for (item in items) {
        val derived = newEmissionFactorMilestone(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planEmissionFactorMilestone(item: EmissionFactorMilestone): Double = emissionFactorProfileWeight(1, true)
