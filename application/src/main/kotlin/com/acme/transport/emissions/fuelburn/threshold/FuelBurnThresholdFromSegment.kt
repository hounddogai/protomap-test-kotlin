package com.acme.transport.emissions.fuelburn.threshold

import com.acme.transport.emissions.emissionfactor.schedule.EmissionFactorSegment
import com.acme.transport.emissions.emissionfactor.schedule.classifyEmissionFactorSchedule

fun summarizeEmissionFactorSegmentsForThreshold(items: MutableList<EmissionFactorSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveFuelBurnThresholdsFromSegment(items: MutableList<EmissionFactorSegment>): MutableList<FuelBurnThreshold> {
    val out = mutableListOf<FuelBurnThreshold>()
    for (item in items) {
        val derived = newFuelBurnThreshold(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeFuelBurnThreshold(item: FuelBurnThreshold): String = classifyEmissionFactorSchedule(item.weightedValue())
