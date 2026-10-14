package com.acme.transport.emissions.carbonreport.regionalreportrule

import com.acme.transport.emissions.emissionfactor.assignment.EmissionFactorAudit
import com.acme.transport.emissions.emissionfactor.assignment.classifyEmissionFactorClaim

fun summarizeEmissionFactorAuditsForAudit(items: MutableList<EmissionFactorAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveBatchedCarbonReportAuditsFromAudit(items: MutableList<EmissionFactorAudit>): MutableList<BatchedCarbonReportAudit> {
    val out = mutableListOf<BatchedCarbonReportAudit>()
    for (item in items) {
        val derived = newBatchedCarbonReportAudit(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun splitBatchedCarbonReportAudit(item: BatchedCarbonReportAudit): String {
    return classifyEmissionFactorClaim(item.weightedTotal())
}
