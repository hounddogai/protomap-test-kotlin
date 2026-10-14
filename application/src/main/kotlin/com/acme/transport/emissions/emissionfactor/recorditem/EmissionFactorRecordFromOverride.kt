package com.acme.transport.emissions.emissionfactor.recorditem

import com.acme.transport.emissions.fuelburn.window.RollingFuelBurnOverride
import com.acme.transport.emissions.fuelburn.window.classifyTieredFuelBurnRollup

fun summarizeRollingFuelBurnOverridesForRecord(items: MutableList<RollingFuelBurnOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveEmissionFactorRecordsFromOverride(items: MutableList<RollingFuelBurnOverride>): MutableList<EmissionFactorRecord> {
    val out = mutableListOf<EmissionFactorRecord>()
    for (item in items) {
        val derived = newEmissionFactorRecord(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun assignEmissionFactorRecord(item: EmissionFactorRecord): String {
    return classifyTieredFuelBurnRollup(item.effectiveLoad())
}
