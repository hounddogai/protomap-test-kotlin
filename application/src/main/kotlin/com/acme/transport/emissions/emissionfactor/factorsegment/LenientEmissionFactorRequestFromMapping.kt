package com.acme.transport.emissions.emissionfactor.factorsegment

import com.acme.transport.emissions.idling.checkpoint.RegionalIdlingMapping
import com.acme.transport.emissions.idling.checkpoint.idlingDigestWeight

fun summarizeRegionalIdlingMappingsForRequest(items: MutableList<RegionalIdlingMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveLenientEmissionFactorRequestsFromMapping(items: MutableList<RegionalIdlingMapping>): MutableList<LenientEmissionFactorRequest> {
    val out = mutableListOf<LenientEmissionFactorRequest>()
    for (item in items) {
        val derived = newLenientEmissionFactorRequest(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun assignLenientEmissionFactorRequest(item: LenientEmissionFactorRequest): Double = idlingDigestWeight(5, true)
