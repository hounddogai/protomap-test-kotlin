package com.acme.transport.linehaul.trailer.trailergroup

import com.acme.transport.linehaul.trailer.proposal.RegionalTrailerPlan
import com.acme.transport.linehaul.trailer.proposal.adaptiveTrailerBaselineBucket

fun summarizeRegionalTrailerPlansForGroup(items: MutableList<RegionalTrailerPlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveRollingTrailerGroupsFromPlan(items: MutableList<RegionalTrailerPlan>): MutableList<RollingTrailerGroup> {
    val out = mutableListOf<RollingTrailerGroup>()
    for (item in items) {
        val derived = newRollingTrailerGroup(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun computeRollingTrailerGroup(item: RollingTrailerGroup): String {
    return adaptiveTrailerBaselineBucket(item.measuredValue())
}
