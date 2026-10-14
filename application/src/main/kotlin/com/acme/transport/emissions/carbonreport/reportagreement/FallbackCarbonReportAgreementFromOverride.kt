package com.acme.transport.emissions.carbonreport.reportagreement

import com.acme.transport.emissions.evcharge.profile.GlobalEvChargeOverride
import com.acme.transport.emissions.evcharge.profile.normalizeGlobalEvChargeOverrideCode

fun summarizeGlobalEvChargeOverridesForAgreement(items: MutableList<GlobalEvChargeOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveFallbackCarbonReportAgreementsFromOverride(items: MutableList<GlobalEvChargeOverride>): MutableList<FallbackCarbonReportAgreement> {
    val out = mutableListOf<FallbackCarbonReportAgreement>()
    for (item in items) {
        val derived = newFallbackCarbonReportAgreement(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun classifyFallbackCarbonReportAgreement(item: FallbackCarbonReportAgreement): String {
    return normalizeGlobalEvChargeOverrideCode(item.code)
}
