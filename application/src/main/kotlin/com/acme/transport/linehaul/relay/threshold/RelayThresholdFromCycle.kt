package com.acme.transport.linehaul.relay.threshold

import com.acme.transport.linehaul.capacity.capacitycycle.RegionalCapacityCycle
import com.acme.transport.linehaul.capacity.capacitycycle.normalizeRegionalCapacityCycleCode

fun summarizeRegionalCapacityCyclesForThreshold(items: MutableList<RegionalCapacityCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveRelayThresholdsFromCycle(items: MutableList<RegionalCapacityCycle>): MutableList<RelayThreshold> {
    val out = mutableListOf<RelayThreshold>()
    for (item in items) {
        val derived = newRelayThreshold(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeRelayThreshold(item: RelayThreshold): String = normalizeRegionalCapacityCycleCode(item.code)
