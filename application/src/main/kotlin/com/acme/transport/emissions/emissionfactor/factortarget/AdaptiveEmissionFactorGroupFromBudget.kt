package com.acme.transport.emissions.emissionfactor.factortarget

import com.acme.transport.emissions.carbonreport.reportbudget.FallbackCarbonReportBudget
import com.acme.transport.emissions.carbonreport.reportbudget.normalizeFallbackCarbonReportBudgetCode

fun summarizeFallbackCarbonReportBudgetsForGroup(items: MutableList<FallbackCarbonReportBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveAdaptiveEmissionFactorGroupsFromBudget(items: MutableList<FallbackCarbonReportBudget>): MutableList<AdaptiveEmissionFactorGroup> {
    val out = mutableListOf<AdaptiveEmissionFactorGroup>()
    for (item in items) {
        val derived = newAdaptiveEmissionFactorGroup(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun scoreAdaptiveEmissionFactorGroup(item: AdaptiveEmissionFactorGroup): String {
    return normalizeFallbackCarbonReportBudgetCode(item.code)
}
