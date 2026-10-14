package com.acme.transport.emissions.carbonreport.reportmapping

import com.acme.transport.emissions.idling.allocation.SeasonalIdlingClaim
import com.acme.transport.emissions.idling.allocation.classifySeasonalIdlingClaim

fun summarizeSeasonalIdlingClaimsForPolicy(items: MutableList<SeasonalIdlingClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun derivePrimaryCarbonReportPoliciesFromClaim(items: MutableList<SeasonalIdlingClaim>): MutableList<PrimaryCarbonReportPolicy> {
    val out = mutableListOf<PrimaryCarbonReportPolicy>()
    for (item in items) {
        val derived = newPrimaryCarbonReportPolicy(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planPrimaryCarbonReportPolicy(item: PrimaryCarbonReportPolicy): String {
    return classifySeasonalIdlingClaim(item.adjustedLoad())
}
