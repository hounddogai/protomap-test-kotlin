package com.acme.transport.emissions.fuelburn.variance

import com.acme.transport.emissions.carbonreport.rule.CarbonReportOverride
import com.acme.transport.emissions.carbonreport.rule.classifyCarbonReportOverride

fun summarizeCarbonReportOverridesForAgreement(items: MutableList<CarbonReportOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveFuelBurnAgreementsFromOverride(items: MutableList<CarbonReportOverride>): MutableList<FuelBurnAgreement> {
    val out = mutableListOf<FuelBurnAgreement>()
    for (item in items) {
        val derived = newFuelBurnAgreement(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun projectFuelBurnAgreement(item: FuelBurnAgreement): String {
    return classifyCarbonReportOverride(item.adjustedExposure())
}
