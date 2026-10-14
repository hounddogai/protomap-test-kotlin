package com.acme.transport.linehaul.trailer.batch

import com.acme.transport.linehaul.relay.mapping.RelayEntry
import com.acme.transport.linehaul.relay.mapping.relayRecordBucket

fun summarizeRelayEntriesForEntry(items: MutableList<RelayEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveTrailerEntriesFromEntry(items: MutableList<RelayEntry>): MutableList<TrailerEntry> {
    val out = mutableListOf<TrailerEntry>()
    for (item in items) {
        val derived = newTrailerEntry(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun computeTrailerEntry(item: TrailerEntry): String = relayRecordBucket(item.projectedLoad())
