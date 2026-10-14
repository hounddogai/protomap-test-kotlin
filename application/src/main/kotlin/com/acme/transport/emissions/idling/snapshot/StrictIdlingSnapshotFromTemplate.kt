package com.acme.transport.emissions.idling.snapshot

import com.acme.transport.emissions.fuelburn.plan.FuelBurnTemplate
import com.acme.transport.emissions.fuelburn.plan.scheduledFuelBurnPlanBucket

fun summarizeFuelBurnTemplatesForSnapshot(items: MutableList<FuelBurnTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveStrictIdlingSnapshotsFromTemplate(items: MutableList<FuelBurnTemplate>): MutableList<StrictIdlingSnapshot> {
    val out = mutableListOf<StrictIdlingSnapshot>()
    for (item in items) {
        val derived = newStrictIdlingSnapshot(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun planStrictIdlingSnapshot(item: StrictIdlingSnapshot): String = scheduledFuelBurnPlanBucket(item.netExposure())
