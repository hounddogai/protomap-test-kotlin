package com.acme.transport.emissions.idling.allocation

import com.acme.transport.emissions.emissionfactor.factorpreference.SeasonalEmissionFactorPreference
import com.acme.transport.emissions.emissionfactor.factorpreference.batchedEmissionFactorPolicyBucket

fun summarizeSeasonalEmissionFactorPreferencesForClaim(items: MutableList<SeasonalEmissionFactorPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveSeasonalIdlingClaimsFromPreference(items: MutableList<SeasonalEmissionFactorPreference>): MutableList<SeasonalIdlingClaim> {
    val out = mutableListOf<SeasonalIdlingClaim>()
    for (item in items) {
        val derived = newSeasonalIdlingClaim(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun planSeasonalIdlingClaim(item: SeasonalIdlingClaim): String {
    return batchedEmissionFactorPolicyBucket(item.projectedLoad())
}
