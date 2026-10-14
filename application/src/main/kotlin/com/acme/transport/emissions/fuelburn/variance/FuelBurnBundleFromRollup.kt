package com.acme.transport.emissions.fuelburn.variance

import com.acme.transport.emissions.idling.milestone.IdlingRollup
import com.acme.transport.emissions.idling.milestone.normalizeIdlingAssignmentCode

fun summarizeIdlingRollupsForBundle(items: MutableList<IdlingRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveFuelBurnBundlesFromRollup(items: MutableList<IdlingRollup>): MutableList<FuelBurnBundle> {
    val out = mutableListOf<FuelBurnBundle>()
    for (item in items) {
        val derived = newFuelBurnBundle(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleFuelBurnBundle(item: FuelBurnBundle): String = normalizeIdlingAssignmentCode(item.code)
