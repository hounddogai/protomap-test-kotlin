package com.acme.transport.emissions.fuelburn.ledger

import com.acme.transport.emissions.idling.window.AdaptiveIdlingDigest
import com.acme.transport.emissions.idling.window.idlingWindowBucket

fun summarizeAdaptiveIdlingDigestsForLedger(items: MutableList<AdaptiveIdlingDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveFuelBurnLedgersFromDigest(items: MutableList<AdaptiveIdlingDigest>): MutableList<FuelBurnLedger> {
    val out = mutableListOf<FuelBurnLedger>()
    for (item in items) {
        val derived = newFuelBurnLedger(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeFuelBurnLedger(item: FuelBurnLedger): String = idlingWindowBucket(item.weightedValue())
