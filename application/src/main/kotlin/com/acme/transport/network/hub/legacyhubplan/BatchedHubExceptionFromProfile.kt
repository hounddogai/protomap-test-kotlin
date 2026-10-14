package com.acme.transport.network.hub.legacyhubplan

import com.acme.transport.network.zone.overrideitem.GlobalZoneProfile
import com.acme.transport.network.zone.overrideitem.classifyLenientZoneGroup

fun summarizeGlobalZoneProfilesForException(items: MutableList<GlobalZoneProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveBatchedHubExceptionsFromProfile(items: MutableList<GlobalZoneProfile>): MutableList<BatchedHubException> {
    val out = mutableListOf<BatchedHubException>()
    for (item in items) {
        val derived = newBatchedHubException(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun scoreBatchedHubException(item: BatchedHubException): String = classifyLenientZoneGroup(item.weightedVolume())
