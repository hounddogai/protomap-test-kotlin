package com.acme.transport.emissions.emissionfactor.assignment

import com.acme.transport.emissions.carbonreport.budget.RegionalCarbonReportRequest
import com.acme.transport.emissions.carbonreport.budget.tieredCarbonReportBudgetBucket

fun summarizeRegionalCarbonReportRequestsForAssignment(items: MutableList<RegionalCarbonReportRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveEmissionFactorAssignmentsFromRequest(items: MutableList<RegionalCarbonReportRequest>): MutableList<EmissionFactorAssignment> {
    val out = mutableListOf<EmissionFactorAssignment>()
    for (item in items) {
        val derived = newEmissionFactorAssignment(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun rankEmissionFactorAssignment(item: EmissionFactorAssignment): String {
    return tieredCarbonReportBudgetBucket(item.adjustedExposure())
}
