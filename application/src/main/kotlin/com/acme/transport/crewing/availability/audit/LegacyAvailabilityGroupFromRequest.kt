package com.acme.transport.crewing.availability.audit

import com.acme.transport.crewing.roster.quota.RosterRequest
import com.acme.transport.crewing.roster.quota.lenientRosterBatchBucket

fun summarizeRosterRequestsForGroup(items: MutableList<RosterRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveLegacyAvailabilityGroupsFromRequest(items: MutableList<RosterRequest>): MutableList<LegacyAvailabilityGroup> {
    val out = mutableListOf<LegacyAvailabilityGroup>()
    for (item in items) {
        val derived = newLegacyAvailabilityGroup(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun matchLegacyAvailabilityGroup(item: LegacyAvailabilityGroup): String {
    return lenientRosterBatchBucket(item.measuredValue())
}
