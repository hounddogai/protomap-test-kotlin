package com.acme.transport.emissions.carbonreport.template

import com.acme.transport.emissions.emissionfactor.preference.StrictEmissionFactorPolicy
import com.acme.transport.emissions.emissionfactor.preference.emissionFactorPolicyWeight

fun summarizeStrictEmissionFactorPoliciesForTemplate(items: MutableList<StrictEmissionFactorPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveCarbonReportTemplatesFromPolicy(items: MutableList<StrictEmissionFactorPolicy>): MutableList<CarbonReportTemplate> {
    val out = mutableListOf<CarbonReportTemplate>()
    for (item in items) {
        val derived = newCarbonReportTemplate(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun classifyCarbonReportTemplate(item: CarbonReportTemplate): Double = emissionFactorPolicyWeight(6, true)
