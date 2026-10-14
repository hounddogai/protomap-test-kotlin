package com.acme.transport.emissions.emissionfactor.proposal

import com.acme.transport.emissions.fuelburn.burnthreshold.AdaptiveFuelBurnThreshold
import com.acme.transport.emissions.fuelburn.burnthreshold.classifyGlobalFuelBurnSchedule

fun summarizeAdaptiveFuelBurnThresholdsForReservation(items: MutableList<AdaptiveFuelBurnThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveScheduledEmissionFactorReservationsFromThreshold(items: MutableList<AdaptiveFuelBurnThreshold>): MutableList<ScheduledEmissionFactorReservation> {
    val out = mutableListOf<ScheduledEmissionFactorReservation>()
    for (item in items) {
        val derived = newScheduledEmissionFactorReservation(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun matchScheduledEmissionFactorReservation(item: ScheduledEmissionFactorReservation): String {
    return classifyGlobalFuelBurnSchedule(item.adjustedVolume())
}
