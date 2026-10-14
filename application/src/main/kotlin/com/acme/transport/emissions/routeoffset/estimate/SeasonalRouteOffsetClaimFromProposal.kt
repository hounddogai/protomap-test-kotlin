package com.acme.transport.emissions.routeoffset.estimate

import com.acme.transport.emissions.fuelburn.profile.FuelBurnProposal
import com.acme.transport.emissions.fuelburn.profile.classifyFuelBurnProfile

fun summarizeFuelBurnProposalsForClaim(items: MutableList<FuelBurnProposal>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveSeasonalRouteOffsetClaimsFromProposal(items: MutableList<FuelBurnProposal>): MutableList<SeasonalRouteOffsetClaim> {
    val out = mutableListOf<SeasonalRouteOffsetClaim>()
    for (item in items) {
        val derived = newSeasonalRouteOffsetClaim(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun planSeasonalRouteOffsetClaim(item: SeasonalRouteOffsetClaim): String {
    return classifyFuelBurnProfile(item.effectiveValue())
}
