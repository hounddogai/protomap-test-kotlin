package com.acme.transport.emissions.fuelburn.agreement

import com.acme.transport.emissions.emissionfactor.recorditem.StrictEmissionFactorAllocation
import com.acme.transport.emissions.emissionfactor.recorditem.normalizeStrictEmissionFactorAllocationCode

fun summarizeStrictEmissionFactorAllocationsForBudget(items: MutableList<StrictEmissionFactorAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveLenientFuelBurnBudgetsFromAllocation(items: MutableList<StrictEmissionFactorAllocation>): MutableList<LenientFuelBurnBudget> {
    val out = mutableListOf<LenientFuelBurnBudget>()
    for (item in items) {
        val derived = newLenientFuelBurnBudget(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun estimateLenientFuelBurnBudget(item: LenientFuelBurnBudget): String {
    return normalizeStrictEmissionFactorAllocationCode(item.code)
}
