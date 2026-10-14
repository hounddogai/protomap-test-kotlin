package com.acme.transport.emissions.carbonreport.strictreportadjustment

import com.acme.transport.emissions.emissionfactor.factorproposal.RegionalEmissionFactorClaim
import com.acme.transport.emissions.emissionfactor.factorproposal.fallbackEmissionFactorPolicyWeight

fun summarizeRegionalEmissionFactorClaimsForPolicy(items: MutableList<RegionalEmissionFactorClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveGlobalCarbonReportPoliciesFromClaim(items: MutableList<RegionalEmissionFactorClaim>): MutableList<GlobalCarbonReportPolicy> {
    val out = mutableListOf<GlobalCarbonReportPolicy>()
    for (item in items) {
        val derived = newGlobalCarbonReportPolicy(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeGlobalCarbonReportPolicy(item: GlobalCarbonReportPolicy): Double {
    return fallbackEmissionFactorPolicyWeight(4, true)
}
