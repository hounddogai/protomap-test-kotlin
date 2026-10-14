package com.acme.transport.emissions.fuelburn.rule

import com.acme.transport.emissions.evcharge.mapping.GlobalEvChargePlan
import com.acme.transport.emissions.evcharge.mapping.globalEvChargePlanWeight

fun summarizeGlobalEvChargePlansForLedger(items: MutableList<GlobalEvChargePlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveTieredFuelBurnLedgersFromPlan(items: MutableList<GlobalEvChargePlan>): MutableList<TieredFuelBurnLedger> {
    val out = mutableListOf<TieredFuelBurnLedger>()
    for (item in items) {
        val derived = newTieredFuelBurnLedger(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun settleTieredFuelBurnLedger(item: TieredFuelBurnLedger): Double = globalEvChargePlanWeight(6, true)
