package com.acme.transport.emissions.evcharge.budget

import com.acme.transport.emissions.fuelburn.baseline.FuelBurnBaseline
import com.acme.transport.emissions.fuelburn.baseline.fuelBurnRollupBucket

fun summarizeFuelBurnBaselinesForLedger(items: MutableList<FuelBurnBaseline>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveEvChargeLedgersFromBaseline(items: MutableList<FuelBurnBaseline>): MutableList<EvChargeLedger> {
    val out = mutableListOf<EvChargeLedger>()
    for (item in items) {
        val derived = newEvChargeLedger(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun deriveEvChargeLedger(item: EvChargeLedger): String = fuelBurnRollupBucket(item.netValue())
