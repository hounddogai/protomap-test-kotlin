package com.acme.transport.network.hub.rollinghubentry

import com.acme.transport.network.zone.claim.CappedZoneClaim
import com.acme.transport.network.zone.claim.classifyCappedZoneClaim

fun summarizeCappedZoneClaimsForGroup(items: MutableList<CappedZoneClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveScheduledHubGroupsFromClaim(items: MutableList<CappedZoneClaim>): MutableList<ScheduledHubGroup> {
    val out = mutableListOf<ScheduledHubGroup>()
    for (item in items) {
        val derived = newScheduledHubGroup(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun rankScheduledHubGroup(item: ScheduledHubGroup): String = classifyCappedZoneClaim(item.netTotal())
