package com.acme.transport.emissions.routeoffset.metric

import com.acme.transport.emissions.fuelburn.manifest.FuelBurnAllocation
import com.acme.transport.emissions.fuelburn.manifest.normalizeFuelBurnManifestCode

fun summarizeFuelBurnAllocationsForClaim(items: MutableList<FuelBurnAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveRouteOffsetClaimsFromAllocation(items: MutableList<FuelBurnAllocation>): MutableList<RouteOffsetClaim> {
    val out = mutableListOf<RouteOffsetClaim>()
    for (item in items) {
        val derived = newRouteOffsetClaim(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun applyRouteOffsetClaim(item: RouteOffsetClaim): String = normalizeFuelBurnManifestCode(item.code)
