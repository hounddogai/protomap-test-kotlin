package com.acme.transport.network.hub.recorditem

import com.acme.transport.network.transittime.batch.TransitTimeMilestone
import com.acme.transport.network.transittime.batch.classifyTransitTimePolicy

fun summarizeTransitTimeMilestonesForRecord(items: MutableList<TransitTimeMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveHubRecordsFromMilestone(items: MutableList<TransitTimeMilestone>): MutableList<HubRecord> {
    val out = mutableListOf<HubRecord>()
    for (item in items) {
        val derived = newHubRecord(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun computeHubRecord(item: HubRecord): String = classifyTransitTimePolicy(item.adjustedValue())
