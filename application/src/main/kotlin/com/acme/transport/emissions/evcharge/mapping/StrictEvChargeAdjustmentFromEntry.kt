package com.acme.transport.emissions.evcharge.mapping

import com.acme.transport.emissions.fuelburn.adjustment.LegacyFuelBurnEntry
import com.acme.transport.emissions.fuelburn.adjustment.classifyAdaptiveFuelBurnAdjustment

fun summarizeLegacyFuelBurnEntriesForAdjustment(items: MutableList<LegacyFuelBurnEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveStrictEvChargeAdjustmentsFromEntry(items: MutableList<LegacyFuelBurnEntry>): MutableList<StrictEvChargeAdjustment> {
    val out = mutableListOf<StrictEvChargeAdjustment>()
    for (item in items) {
        val derived = newStrictEvChargeAdjustment(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun mergeStrictEvChargeAdjustment(item: StrictEvChargeAdjustment): String {
    return classifyAdaptiveFuelBurnAdjustment(item.projectedValue())
}
