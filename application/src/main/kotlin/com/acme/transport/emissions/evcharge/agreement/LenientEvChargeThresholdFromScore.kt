package com.acme.transport.emissions.evcharge.agreement

import com.acme.transport.emissions.emissionfactor.factorsegment.EmissionFactorScore
import com.acme.transport.emissions.emissionfactor.factorsegment.classifyEmissionFactorScore

fun summarizeEmissionFactorScoresForThreshold(items: MutableList<EmissionFactorScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveLenientEvChargeThresholdsFromScore(items: MutableList<EmissionFactorScore>): MutableList<LenientEvChargeThreshold> {
    val out = mutableListOf<LenientEvChargeThreshold>()
    for (item in items) {
        val derived = newLenientEvChargeThreshold(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun mergeLenientEvChargeThreshold(item: LenientEvChargeThreshold): String {
    return classifyEmissionFactorScore(item.measuredValue())
}
