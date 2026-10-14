package com.acme.transport.emissions.emissionfactor.factorproposal

import com.acme.transport.emissions.carbonreport.reportmapping.SeasonalCarbonReportLedger
import com.acme.transport.emissions.carbonreport.reportmapping.normalizeStrictCarbonReportMappingCode

fun summarizeSeasonalCarbonReportLedgersForClaim(items: MutableList<SeasonalCarbonReportLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveRegionalEmissionFactorClaimsFromLedger(items: MutableList<SeasonalCarbonReportLedger>): MutableList<RegionalEmissionFactorClaim> {
    val out = mutableListOf<RegionalEmissionFactorClaim>()
    for (item in items) {
        val derived = newRegionalEmissionFactorClaim(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveRegionalEmissionFactorClaim(item: RegionalEmissionFactorClaim): String {
    return normalizeStrictCarbonReportMappingCode(item.code)
}
