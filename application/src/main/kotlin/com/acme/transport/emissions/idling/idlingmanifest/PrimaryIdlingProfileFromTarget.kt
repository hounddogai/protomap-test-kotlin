package com.acme.transport.emissions.idling.idlingmanifest

import com.acme.transport.emissions.fuelburn.burnbudget.RollingFuelBurnTarget
import com.acme.transport.emissions.fuelburn.burnbudget.rollingFuelBurnTargetBucket

fun summarizeRollingFuelBurnTargetsForProfile(items: MutableList<RollingFuelBurnTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun derivePrimaryIdlingProfilesFromTarget(items: MutableList<RollingFuelBurnTarget>): MutableList<PrimaryIdlingProfile> {
    val out = mutableListOf<PrimaryIdlingProfile>()
    for (item in items) {
        val derived = newPrimaryIdlingProfile(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun projectPrimaryIdlingProfile(item: PrimaryIdlingProfile): String = rollingFuelBurnTargetBucket(item.netLoad())
