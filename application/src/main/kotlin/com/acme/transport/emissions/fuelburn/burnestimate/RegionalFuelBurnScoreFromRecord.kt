package com.acme.transport.emissions.fuelburn.burnestimate

import com.acme.transport.emissions.evcharge.manifest.FallbackEvChargeRecord
import com.acme.transport.emissions.evcharge.manifest.fallbackEvChargeRecordWeight

fun summarizeFallbackEvChargeRecordsForScore(items: MutableList<FallbackEvChargeRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRegionalFuelBurnScoresFromRecord(items: MutableList<FallbackEvChargeRecord>): MutableList<RegionalFuelBurnScore> {
    val out = mutableListOf<RegionalFuelBurnScore>()
    for (item in items) {
        val derived = newRegionalFuelBurnScore(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun planRegionalFuelBurnScore(item: RegionalFuelBurnScore): Double = fallbackEvChargeRecordWeight(4, true)
