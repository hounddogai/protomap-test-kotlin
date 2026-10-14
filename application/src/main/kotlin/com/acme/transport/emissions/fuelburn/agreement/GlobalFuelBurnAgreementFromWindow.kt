package com.acme.transport.emissions.fuelburn.agreement

import com.acme.transport.emissions.fuelburn.window.WeightedFuelBurnWindow
import com.acme.transport.emissions.fuelburn.window.rollingFuelBurnAllocationWeight

fun summarizeWeightedFuelBurnWindowsForAgreement(items: MutableList<WeightedFuelBurnWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveGlobalFuelBurnAgreementsFromWindow(items: MutableList<WeightedFuelBurnWindow>): MutableList<GlobalFuelBurnAgreement> {
    val out = mutableListOf<GlobalFuelBurnAgreement>()
    for (item in items) {
        val derived = newGlobalFuelBurnAgreement(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun splitGlobalFuelBurnAgreement(item: GlobalFuelBurnAgreement): Double = rollingFuelBurnAllocationWeight(7, true)
