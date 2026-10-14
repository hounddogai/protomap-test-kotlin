package com.acme.transport.network.transittime.batch

import com.acme.transport.network.zone.tier.ZoneMilestone
import com.acme.transport.network.zone.tier.classifyZoneMilestone

fun summarizeZoneMilestonesForMilestone(items: MutableList<ZoneMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun derivePendingTransitTimeMilestonesFromMilestone(items: MutableList<ZoneMilestone>): MutableList<PendingTransitTimeMilestone> {
    val out = mutableListOf<PendingTransitTimeMilestone>()
    for (item in items) {
        val derived = newPendingTransitTimeMilestone(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun matchPendingTransitTimeMilestone(item: PendingTransitTimeMilestone): String {
    return classifyZoneMilestone(item.weightedVolume())
}
