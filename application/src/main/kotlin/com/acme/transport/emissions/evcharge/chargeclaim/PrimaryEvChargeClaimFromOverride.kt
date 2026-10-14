package com.acme.transport.emissions.evcharge.chargeclaim

import com.acme.transport.emissions.fuelburn.window.RollingFuelBurnOverride
import com.acme.transport.emissions.fuelburn.window.rollingFuelBurnAllocationBucket

fun summarizeRollingFuelBurnOverridesForClaim(items: MutableList<RollingFuelBurnOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun derivePrimaryEvChargeClaimsFromOverride(items: MutableList<RollingFuelBurnOverride>): MutableList<PrimaryEvChargeClaim> {
    val out = mutableListOf<PrimaryEvChargeClaim>()
    for (item in items) {
        val derived = newPrimaryEvChargeClaim(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun planPrimaryEvChargeClaim(item: PrimaryEvChargeClaim): String {
    return rollingFuelBurnAllocationBucket(item.measuredValue())
}
