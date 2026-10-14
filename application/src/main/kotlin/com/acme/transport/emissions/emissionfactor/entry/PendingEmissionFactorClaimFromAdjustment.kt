package com.acme.transport.emissions.emissionfactor.entry

import com.acme.transport.emissions.carbonreport.strictreportadjustment.PrimaryCarbonReportAdjustment
import com.acme.transport.emissions.carbonreport.strictreportadjustment.classifyGlobalCarbonReportPolicy

fun summarizePrimaryCarbonReportAdjustmentsForClaim(items: MutableList<PrimaryCarbonReportAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun derivePendingEmissionFactorClaimsFromAdjustment(items: MutableList<PrimaryCarbonReportAdjustment>): MutableList<PendingEmissionFactorClaim> {
    val out = mutableListOf<PendingEmissionFactorClaim>()
    for (item in items) {
        val derived = newPendingEmissionFactorClaim(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun planPendingEmissionFactorClaim(item: PendingEmissionFactorClaim): String {
    return classifyGlobalCarbonReportPolicy(item.effectiveValue())
}
