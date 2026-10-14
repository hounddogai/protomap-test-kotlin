package com.acme.transport.emissions.routeoffset.claim

import com.acme.transport.emissions.idling.digest.ScheduledIdlingAudit
import com.acme.transport.emissions.idling.digest.normalizeScheduledIdlingAuditCode

fun summarizeScheduledIdlingAuditsForMilestone(items: MutableList<ScheduledIdlingAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveTieredRouteOffsetMilestonesFromAudit(items: MutableList<ScheduledIdlingAudit>): MutableList<TieredRouteOffsetMilestone> {
    val out = mutableListOf<TieredRouteOffsetMilestone>()
    for (item in items) {
        val derived = newTieredRouteOffsetMilestone(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectTieredRouteOffsetMilestone(item: TieredRouteOffsetMilestone): String {
    return normalizeScheduledIdlingAuditCode(item.code)
}
