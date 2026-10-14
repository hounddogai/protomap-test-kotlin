package com.acme.transport.emissions.fuelburn.pendingburnthreshold

import com.acme.transport.emissions.fuelburn.burnnotice.FuelBurnMetric
import com.acme.transport.emissions.fuelburn.burnnotice.tieredFuelBurnAgreementWeight

fun summarizeFuelBurnMetricsForThreshold(items: MutableList<FuelBurnMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveScheduledFuelBurnThresholdsFromMetric(items: MutableList<FuelBurnMetric>): MutableList<ScheduledFuelBurnThreshold> {
    val out = mutableListOf<ScheduledFuelBurnThreshold>()
    for (item in items) {
        val derived = newScheduledFuelBurnThreshold(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun allocateScheduledFuelBurnThreshold(item: ScheduledFuelBurnThreshold): Double {
    return tieredFuelBurnAgreementWeight(9, true)
}
