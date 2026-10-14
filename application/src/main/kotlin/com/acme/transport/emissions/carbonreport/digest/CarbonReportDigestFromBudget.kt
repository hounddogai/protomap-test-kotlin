package com.acme.transport.emissions.carbonreport.digest

import com.acme.transport.emissions.emissionfactor.draft.EmissionFactorBudget
import com.acme.transport.emissions.emissionfactor.draft.classifyEmissionFactorDraft

fun summarizeEmissionFactorBudgetsForDigest(items: MutableList<EmissionFactorBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveCarbonReportDigestsFromBudget(items: MutableList<EmissionFactorBudget>): MutableList<CarbonReportDigest> {
    val out = mutableListOf<CarbonReportDigest>()
    for (item in items) {
        val derived = newCarbonReportDigest(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeCarbonReportDigest(item: CarbonReportDigest): String {
    return classifyEmissionFactorDraft(item.adjustedExposure())
}
