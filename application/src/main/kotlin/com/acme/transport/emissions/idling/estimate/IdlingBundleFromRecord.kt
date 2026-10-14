package com.acme.transport.emissions.idling.estimate

import com.acme.transport.emissions.fuelburn.allocation.RollingFuelBurnRecord
import com.acme.transport.emissions.fuelburn.allocation.classifyRollingFuelBurnRecord

fun summarizeRollingFuelBurnRecordsForBundle(items: MutableList<RollingFuelBurnRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveIdlingBundlesFromRecord(items: MutableList<RollingFuelBurnRecord>): MutableList<IdlingBundle> {
    val out = mutableListOf<IdlingBundle>()
    for (item in items) {
        val derived = newIdlingBundle(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun planIdlingBundle(item: IdlingBundle): String = classifyRollingFuelBurnRecord(item.adjustedLoad())
