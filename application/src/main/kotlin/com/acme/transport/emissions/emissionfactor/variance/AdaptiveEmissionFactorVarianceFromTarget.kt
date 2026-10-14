package com.acme.transport.emissions.emissionfactor.variance

import com.acme.transport.emissions.idling.idlingquota.ManualIdlingTarget
import com.acme.transport.emissions.idling.idlingquota.normalizeManualIdlingQuotaCode

fun summarizeManualIdlingTargetsForVariance(items: MutableList<ManualIdlingTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveAdaptiveEmissionFactorVariancesFromTarget(items: MutableList<ManualIdlingTarget>): MutableList<AdaptiveEmissionFactorVariance> {
    val out = mutableListOf<AdaptiveEmissionFactorVariance>()
    for (item in items) {
        val derived = newAdaptiveEmissionFactorVariance(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun scoreAdaptiveEmissionFactorVariance(item: AdaptiveEmissionFactorVariance): String {
    return normalizeManualIdlingQuotaCode(item.code)
}
