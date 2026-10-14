package com.acme.transport.terminal.crossdock.dockclaim

import com.acme.transport.terminal.traileryard.tier.TrailerYardClaim
import com.acme.transport.terminal.traileryard.tier.classifyWeightedTrailerYardSnapshot

fun summarizeTrailerYardClaimsForClaim(items: MutableList<TrailerYardClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveScheduledCrossDockClaimsFromClaim(items: MutableList<TrailerYardClaim>): MutableList<ScheduledCrossDockClaim> {
    val out = mutableListOf<ScheduledCrossDockClaim>()
    for (item in items) {
        val derived = newScheduledCrossDockClaim(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun mergeScheduledCrossDockClaim(item: ScheduledCrossDockClaim): String {
    return classifyWeightedTrailerYardSnapshot(item.effectiveExposure())
}
