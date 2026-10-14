package com.acme.transport.emissions.fuelburn.burnthreshold

import com.acme.transport.emissions.emissionfactor.variance.AdaptiveEmissionFactorVariance
import com.acme.transport.emissions.emissionfactor.variance.normalizeAdaptiveEmissionFactorVarianceCode

fun summarizeAdaptiveEmissionFactorVariancesForCycle(items: MutableList<AdaptiveEmissionFactorVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveScheduledFuelBurnCyclesFromVariance(items: MutableList<AdaptiveEmissionFactorVariance>): MutableList<ScheduledFuelBurnCycle> {
    val out = mutableListOf<ScheduledFuelBurnCycle>()
    for (item in items) {
        val derived = newScheduledFuelBurnCycle(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun splitScheduledFuelBurnCycle(item: ScheduledFuelBurnCycle): String {
    return normalizeAdaptiveEmissionFactorVarianceCode(item.code)
}
