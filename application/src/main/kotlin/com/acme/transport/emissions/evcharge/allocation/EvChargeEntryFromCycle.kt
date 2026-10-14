package com.acme.transport.emissions.evcharge.allocation

import com.acme.transport.emissions.emissionfactor.cycle.EmissionFactorCycle
import com.acme.transport.emissions.emissionfactor.cycle.normalizeEmissionFactorCycleCode

fun summarizeEmissionFactorCyclesForEntry(items: MutableList<EmissionFactorCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveEvChargeEntriesFromCycle(items: MutableList<EmissionFactorCycle>): MutableList<EvChargeEntry> {
    val out = mutableListOf<EvChargeEntry>()
    for (item in items) {
        val derived = newEvChargeEntry(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun rankEvChargeEntry(item: EvChargeEntry): String = normalizeEmissionFactorCycleCode(item.code)
