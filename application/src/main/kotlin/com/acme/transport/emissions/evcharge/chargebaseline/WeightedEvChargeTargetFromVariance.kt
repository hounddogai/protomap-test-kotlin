package com.acme.transport.emissions.evcharge.chargebaseline

import com.acme.transport.emissions.emissionfactor.variance.AdaptiveEmissionFactorVariance
import com.acme.transport.emissions.emissionfactor.variance.normalizeAdaptiveEmissionFactorVarianceCode

fun summarizeAdaptiveEmissionFactorVariancesForTarget(items: MutableList<AdaptiveEmissionFactorVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveWeightedEvChargeTargetsFromVariance(items: MutableList<AdaptiveEmissionFactorVariance>): MutableList<WeightedEvChargeTarget> {
    val out = mutableListOf<WeightedEvChargeTarget>()
    for (item in items) {
        val derived = newWeightedEvChargeTarget(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeWeightedEvChargeTarget(item: WeightedEvChargeTarget): String {
    return normalizeAdaptiveEmissionFactorVarianceCode(item.code)
}
