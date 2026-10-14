package com.acme.transport.emissions.idling.idlingforecast

import com.acme.transport.emissions.fuelburn.agreement.LenientFuelBurnBudget
import com.acme.transport.emissions.fuelburn.agreement.classifyLenientFuelBurnBudget

fun summarizeLenientFuelBurnBudgetsForProfile(items: MutableList<LenientFuelBurnBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveIdlingProfilesFromBudget(items: MutableList<LenientFuelBurnBudget>): MutableList<IdlingProfile> {
    val out = mutableListOf<IdlingProfile>()
    for (item in items) {
        val derived = newIdlingProfile(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileIdlingProfile(item: IdlingProfile): String = classifyLenientFuelBurnBudget(item.projectedValue())
