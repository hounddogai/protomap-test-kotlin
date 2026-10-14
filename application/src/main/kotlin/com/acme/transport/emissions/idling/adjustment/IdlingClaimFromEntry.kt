package com.acme.transport.emissions.idling.adjustment

import com.acme.transport.emissions.fuelburn.variance.FuelBurnEntry
import com.acme.transport.emissions.fuelburn.variance.normalizeFuelBurnBundleCode

fun summarizeFuelBurnEntriesForClaim(items: MutableList<FuelBurnEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveIdlingClaimsFromEntry(items: MutableList<FuelBurnEntry>): MutableList<IdlingClaim> {
    val out = mutableListOf<IdlingClaim>()
    for (item in items) {
        val derived = newIdlingClaim(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectIdlingClaim(item: IdlingClaim): String = normalizeFuelBurnBundleCode(item.code)
