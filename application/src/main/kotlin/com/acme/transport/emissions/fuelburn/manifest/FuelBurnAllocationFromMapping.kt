package com.acme.transport.emissions.fuelburn.manifest

import com.acme.transport.emissions.fuelburn.ledger.WeightedFuelBurnMapping
import com.acme.transport.emissions.fuelburn.ledger.classifyFuelBurnLedger

fun summarizeWeightedFuelBurnMappingsForAllocation(items: MutableList<WeightedFuelBurnMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveFuelBurnAllocationsFromMapping(items: MutableList<WeightedFuelBurnMapping>): MutableList<FuelBurnAllocation> {
    val out = mutableListOf<FuelBurnAllocation>()
    for (item in items) {
        val derived = newFuelBurnAllocation(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankFuelBurnAllocation(item: FuelBurnAllocation): String = classifyFuelBurnLedger(item.projectedValue())
