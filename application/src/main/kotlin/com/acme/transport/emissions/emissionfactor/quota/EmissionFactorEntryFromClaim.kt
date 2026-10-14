package com.acme.transport.emissions.emissionfactor.quota

import com.acme.transport.emissions.idling.allocation.SeasonalIdlingClaim
import com.acme.transport.emissions.idling.allocation.classifySeasonalIdlingClaim

fun summarizeSeasonalIdlingClaimsForEntry(items: MutableList<SeasonalIdlingClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveEmissionFactorEntriesFromClaim(items: MutableList<SeasonalIdlingClaim>): MutableList<EmissionFactorEntry> {
    val out = mutableListOf<EmissionFactorEntry>()
    for (item in items) {
        val derived = newEmissionFactorEntry(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankEmissionFactorEntry(item: EmissionFactorEntry): String = classifySeasonalIdlingClaim(item.netValue())
