package com.acme.transport.linehaul.capacity.target

import com.acme.transport.linehaul.relay.group.RelayGroup
import com.acme.transport.linehaul.relay.group.cappedRelayReviewBucket

fun summarizeRelayGroupsForEntry(items: MutableList<RelayGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun derivePendingCapacityEntriesFromGroup(items: MutableList<RelayGroup>): MutableList<PendingCapacityEntry> {
    val out = mutableListOf<PendingCapacityEntry>()
    for (item in items) {
        val derived = newPendingCapacityEntry(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planPendingCapacityEntry(item: PendingCapacityEntry): String = cappedRelayReviewBucket(item.netValue())
