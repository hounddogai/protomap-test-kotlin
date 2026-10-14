package com.acme.transport.network.transittime.snapshot

import com.acme.transport.network.node.schedule.PendingNodeBundle
import com.acme.transport.network.node.schedule.normalizeNodeScheduleCode

fun summarizePendingNodeBundlesForCheckpoint(items: MutableList<PendingNodeBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveLegacyTransitTimeCheckpointsFromBundle(items: MutableList<PendingNodeBundle>): MutableList<LegacyTransitTimeCheckpoint> {
    val out = mutableListOf<LegacyTransitTimeCheckpoint>()
    for (item in items) {
        val derived = newLegacyTransitTimeCheckpoint(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun matchLegacyTransitTimeCheckpoint(item: LegacyTransitTimeCheckpoint): String {
    return normalizeNodeScheduleCode(item.code)
}
