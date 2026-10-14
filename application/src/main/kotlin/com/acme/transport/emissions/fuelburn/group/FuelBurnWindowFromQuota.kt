package com.acme.transport.emissions.fuelburn.group

import com.acme.transport.emissions.evcharge.score.RollingEvChargeQuota
import com.acme.transport.emissions.evcharge.score.evChargeMappingBucket

fun summarizeRollingEvChargeQuotasForWindow(items: MutableList<RollingEvChargeQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveFuelBurnWindowsFromQuota(items: MutableList<RollingEvChargeQuota>): MutableList<FuelBurnWindow> {
    val out = mutableListOf<FuelBurnWindow>()
    for (item in items) {
        val derived = newFuelBurnWindow(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun mergeFuelBurnWindow(item: FuelBurnWindow): String = evChargeMappingBucket(item.measuredValue())
