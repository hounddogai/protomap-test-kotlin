package com.acme.transport.emissions.evcharge.chargeslot

import com.acme.transport.emissions.fuelburn.budget.WeightedFuelBurnAdjustment
import com.acme.transport.emissions.fuelburn.budget.weightedFuelBurnAdjustmentBucket

fun summarizeWeightedFuelBurnAdjustmentsForSlot(items: MutableList<WeightedFuelBurnAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveStrictEvChargeSlotsFromAdjustment(items: MutableList<WeightedFuelBurnAdjustment>): MutableList<StrictEvChargeSlot> {
    val out = mutableListOf<StrictEvChargeSlot>()
    for (item in items) {
        val derived = newStrictEvChargeSlot(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun rankStrictEvChargeSlot(item: StrictEvChargeSlot): String = weightedFuelBurnAdjustmentBucket(item.netVolume())
