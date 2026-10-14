package com.acme.transport.emissions.fuelburn.ledger

import com.acme.transport.emissions.evcharge.plan.BatchedEvChargeSchedule
import com.acme.transport.emissions.evcharge.plan.batchedEvChargeScheduleWeight

fun summarizeBatchedEvChargeSchedulesForLedger(items: MutableList<BatchedEvChargeSchedule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveFuelBurnLedgersFromSchedule(items: MutableList<BatchedEvChargeSchedule>): MutableList<FuelBurnLedger> {
    val out = mutableListOf<FuelBurnLedger>()
    for (item in items) {
        val derived = newFuelBurnLedger(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun planFuelBurnLedger(item: FuelBurnLedger): Double = batchedEvChargeScheduleWeight(6, true)
