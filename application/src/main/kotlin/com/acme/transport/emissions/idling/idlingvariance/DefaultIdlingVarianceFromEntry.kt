package com.acme.transport.emissions.idling.idlingvariance

import com.acme.transport.emissions.emissionfactor.quota.EmissionFactorEntry
import com.acme.transport.emissions.emissionfactor.quota.classifyEmissionFactorEntry

fun summarizeEmissionFactorEntriesForVariance(items: MutableList<EmissionFactorEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveDefaultIdlingVariancesFromEntry(items: MutableList<EmissionFactorEntry>): MutableList<DefaultIdlingVariance> {
    val out = mutableListOf<DefaultIdlingVariance>()
    for (item in items) {
        val derived = newDefaultIdlingVariance(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeDefaultIdlingVariance(item: DefaultIdlingVariance): String {
    return classifyEmissionFactorEntry(item.netExposure())
}
