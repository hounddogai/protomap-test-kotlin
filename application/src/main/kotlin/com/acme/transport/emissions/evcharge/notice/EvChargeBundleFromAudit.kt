package com.acme.transport.emissions.evcharge.notice

import com.acme.transport.emissions.fuelburn.pendingburnthreshold.RegionalFuelBurnAudit
import com.acme.transport.emissions.fuelburn.pendingburnthreshold.classifyFuelBurnReservation

fun summarizeRegionalFuelBurnAuditsForBundle(items: MutableList<RegionalFuelBurnAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveEvChargeBundlesFromAudit(items: MutableList<RegionalFuelBurnAudit>): MutableList<EvChargeBundle> {
    val out = mutableListOf<EvChargeBundle>()
    for (item in items) {
        val derived = newEvChargeBundle(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignEvChargeBundle(item: EvChargeBundle): String = classifyFuelBurnReservation(item.weightedVolume())
