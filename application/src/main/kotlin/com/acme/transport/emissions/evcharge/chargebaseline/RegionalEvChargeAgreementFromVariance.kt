package com.acme.transport.emissions.evcharge.chargebaseline

import com.acme.transport.emissions.carbonreport.batch.RollingCarbonReportVariance
import com.acme.transport.emissions.carbonreport.batch.classifyRollingCarbonReportVariance

fun summarizeRollingCarbonReportVariancesForAgreement(items: MutableList<RollingCarbonReportVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRegionalEvChargeAgreementsFromVariance(items: MutableList<RollingCarbonReportVariance>): MutableList<RegionalEvChargeAgreement> {
    val out = mutableListOf<RegionalEvChargeAgreement>()
    for (item in items) {
        val derived = newRegionalEvChargeAgreement(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveRegionalEvChargeAgreement(item: RegionalEvChargeAgreement): String {
    return classifyRollingCarbonReportVariance(item.effectiveExposure())
}
