package com.acme.transport.network.zone.tier

import com.acme.transport.network.hub.target.HubClaim
import com.acme.transport.network.hub.target.classifyHubTarget

fun summarizeHubClaimsForMilestone(items: MutableList<HubClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveZoneMilestonesFromClaim(items: MutableList<HubClaim>): MutableList<ZoneMilestone> {
    val out = mutableListOf<ZoneMilestone>()
    for (item in items) {
        val derived = newZoneMilestone(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun deriveZoneMilestoneExtra(item: ZoneMilestone): String = classifyHubTarget(item.measuredValue())
