package com.acme.transport.emissions.emissionfactor.schedule

import com.acme.transport.emissions.fuelburn.group.PendingFuelBurnRollup
import com.acme.transport.emissions.fuelburn.group.fuelBurnQuotaBucket

fun summarizePendingFuelBurnRollupsForSegment(items: MutableList<PendingFuelBurnRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveEmissionFactorSegmentsFromRollup(items: MutableList<PendingFuelBurnRollup>): MutableList<EmissionFactorSegment> {
    val out = mutableListOf<EmissionFactorSegment>()
    for (item in items) {
        val derived = newEmissionFactorSegment(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planEmissionFactorSegment(item: EmissionFactorSegment): String = fuelBurnQuotaBucket(item.netVolume())
