package com.acme.transport.emissions.emissionfactor.request

import com.acme.transport.emissions.fuelburn.request.FuelBurnCycle
import com.acme.transport.emissions.fuelburn.request.normalizeFuelBurnRequestCode

fun summarizeFuelBurnCyclesForBatch(items: MutableList<FuelBurnCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveBatchedEmissionFactorBatchesFromCycle(items: MutableList<FuelBurnCycle>): MutableList<BatchedEmissionFactorBatch> {
    val out = mutableListOf<BatchedEmissionFactorBatch>()
    for (item in items) {
        val derived = newBatchedEmissionFactorBatch(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeBatchedEmissionFactorBatch(item: BatchedEmissionFactorBatch): String {
    return normalizeFuelBurnRequestCode(item.code)
}
