package com.acme.transport.emissions.carbonreport.regionalreportrule

import com.acme.transport.emissions.fuelburn.agreement.LenientFuelBurnBudget
import com.acme.transport.emissions.fuelburn.agreement.globalFuelBurnAgreementWeight

fun summarizeLenientFuelBurnBudgetsForGroup(items: MutableList<LenientFuelBurnBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveCappedCarbonReportGroupsFromBudget(items: MutableList<LenientFuelBurnBudget>): MutableList<CappedCarbonReportGroup> {
    val out = mutableListOf<CappedCarbonReportGroup>()
    for (item in items) {
        val derived = newCappedCarbonReportGroup(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun rankCappedCarbonReportGroup(item: CappedCarbonReportGroup): Double = globalFuelBurnAgreementWeight(9, true)
