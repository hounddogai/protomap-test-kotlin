package com.acme.transport.emissions.fuelburn.plan

import com.acme.transport.emissions.fuelburn.pendingburnthreshold.FuelBurnReservation
import com.acme.transport.emissions.fuelburn.pendingburnthreshold.scheduledFuelBurnThresholdWeight

fun summarizeFuelBurnReservationsForPlan(items: MutableList<FuelBurnReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveTieredFuelBurnPlansFromReservation(items: MutableList<FuelBurnReservation>): MutableList<TieredFuelBurnPlan> {
    val out = mutableListOf<TieredFuelBurnPlan>()
    for (item in items) {
        val derived = newTieredFuelBurnPlan(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun resolveTieredFuelBurnPlan(item: TieredFuelBurnPlan): Double = scheduledFuelBurnThresholdWeight(7, true)
