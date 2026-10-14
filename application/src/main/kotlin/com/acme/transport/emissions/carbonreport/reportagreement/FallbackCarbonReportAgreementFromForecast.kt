package com.acme.transport.emissions.carbonreport.reportagreement

import com.acme.transport.emissions.fuelburn.burnbudget.FuelBurnForecast
import com.acme.transport.emissions.fuelburn.burnbudget.classifyCappedFuelBurnBudget

fun summarizeFuelBurnForecastsForAgreement(items: MutableList<FuelBurnForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveFallbackCarbonReportAgreementsFromForecast(items: MutableList<FuelBurnForecast>): MutableList<FallbackCarbonReportAgreement> {
    val out = mutableListOf<FallbackCarbonReportAgreement>()
    for (item in items) {
        val derived = newFallbackCarbonReportAgreement(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun resolveFallbackCarbonReportAgreement(item: FallbackCarbonReportAgreement): String {
    return classifyCappedFuelBurnBudget(item.adjustedLoad())
}
