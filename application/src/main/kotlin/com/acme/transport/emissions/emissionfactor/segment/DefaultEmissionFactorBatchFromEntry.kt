package com.acme.transport.emissions.emissionfactor.segment

import com.acme.transport.emissions.fuelburn.threshold.StrictFuelBurnEntry
import com.acme.transport.emissions.fuelburn.threshold.strictFuelBurnEntryWeight

fun summarizeStrictFuelBurnEntriesForBatch(items: MutableList<StrictFuelBurnEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveDefaultEmissionFactorBatchesFromEntry(items: MutableList<StrictFuelBurnEntry>): MutableList<DefaultEmissionFactorBatch> {
    val out = mutableListOf<DefaultEmissionFactorBatch>()
    for (item in items) {
        val derived = newDefaultEmissionFactorBatch(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateDefaultEmissionFactorBatch(item: DefaultEmissionFactorBatch): Double {
    return strictFuelBurnEntryWeight(5, true)
}
