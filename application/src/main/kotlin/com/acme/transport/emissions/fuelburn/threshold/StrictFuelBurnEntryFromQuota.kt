package com.acme.transport.emissions.fuelburn.threshold

import com.acme.transport.emissions.fuelburn.group.FuelBurnQuota
import com.acme.transport.emissions.fuelburn.group.normalizeFuelBurnWindowCode

fun summarizeFuelBurnQuotasForEntry(items: MutableList<FuelBurnQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveStrictFuelBurnEntriesFromQuota(items: MutableList<FuelBurnQuota>): MutableList<StrictFuelBurnEntry> {
    val out = mutableListOf<StrictFuelBurnEntry>()
    for (item in items) {
        val derived = newStrictFuelBurnEntry(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun assignStrictFuelBurnEntry(item: StrictFuelBurnEntry): String = normalizeFuelBurnWindowCode(item.code)
