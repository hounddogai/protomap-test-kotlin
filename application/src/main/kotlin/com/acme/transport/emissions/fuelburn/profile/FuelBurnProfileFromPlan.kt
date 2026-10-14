package com.acme.transport.emissions.fuelburn.profile

import com.acme.transport.emissions.idling.variance.IdlingPlan
import com.acme.transport.emissions.idling.variance.idlingPlanBucket

fun summarizeIdlingPlansForProfile(items: MutableList<IdlingPlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveFuelBurnProfilesFromPlan(items: MutableList<IdlingPlan>): MutableList<FuelBurnProfile> {
    val out = mutableListOf<FuelBurnProfile>()
    for (item in items) {
        val derived = newFuelBurnProfile(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun deriveFuelBurnProfile(item: FuelBurnProfile): String = idlingPlanBucket(item.weightedExposure())
