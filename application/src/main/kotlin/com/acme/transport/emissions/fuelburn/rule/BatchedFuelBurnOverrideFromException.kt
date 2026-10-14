package com.acme.transport.emissions.fuelburn.rule

import com.acme.transport.emissions.emissionfactor.rollingfactorsegment.SeasonalEmissionFactorException
import com.acme.transport.emissions.emissionfactor.rollingfactorsegment.pendingEmissionFactorSegmentWeight

fun summarizeSeasonalEmissionFactorExceptionsForOverride(items: MutableList<SeasonalEmissionFactorException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveBatchedFuelBurnOverridesFromException(items: MutableList<SeasonalEmissionFactorException>): MutableList<BatchedFuelBurnOverride> {
    val out = mutableListOf<BatchedFuelBurnOverride>()
    for (item in items) {
        val derived = newBatchedFuelBurnOverride(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveBatchedFuelBurnOverride(item: BatchedFuelBurnOverride): Double {
    return pendingEmissionFactorSegmentWeight(2, true)
}
