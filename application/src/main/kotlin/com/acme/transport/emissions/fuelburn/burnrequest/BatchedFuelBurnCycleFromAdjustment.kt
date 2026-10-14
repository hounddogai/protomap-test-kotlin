package com.acme.transport.emissions.fuelburn.burnrequest

import com.acme.transport.emissions.emissionfactor.factorschedule.DefaultEmissionFactorAdjustment
import com.acme.transport.emissions.emissionfactor.factorschedule.normalizeEmissionFactorDigestCode

fun summarizeDefaultEmissionFactorAdjustmentsForCycle(items: MutableList<DefaultEmissionFactorAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveBatchedFuelBurnCyclesFromAdjustment(items: MutableList<DefaultEmissionFactorAdjustment>): MutableList<BatchedFuelBurnCycle> {
    val out = mutableListOf<BatchedFuelBurnCycle>()
    for (item in items) {
        val derived = newBatchedFuelBurnCycle(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun applyBatchedFuelBurnCycle(item: BatchedFuelBurnCycle): String = normalizeEmissionFactorDigestCode(item.code)
