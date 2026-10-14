package com.acme.transport.network.zone.agreement

import com.acme.transport.network.hub.batch.HubRollup
import com.acme.transport.network.hub.batch.classifyHubBaseline

fun summarizeHubRollupsForCheckpoint(items: MutableList<HubRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveRegionalZoneCheckpointsFromRollup(items: MutableList<HubRollup>): MutableList<RegionalZoneCheckpoint> {
    val out = mutableListOf<RegionalZoneCheckpoint>()
    for (item in items) {
        val derived = newRegionalZoneCheckpoint(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyRegionalZoneCheckpointExtra(item: RegionalZoneCheckpoint): String {
    return classifyHubBaseline(item.projectedExposure())
}
