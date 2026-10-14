package com.acme.transport.linehaul.relay.adjustment

import com.acme.transport.linehaul.relay.mapping.RelayRecord
import com.acme.transport.linehaul.relay.mapping.classifyRelayEntry

fun summarizeRelayRecordsForClaim(items: MutableList<RelayRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveRelayClaimsFromRecord(items: MutableList<RelayRecord>): MutableList<RelayClaim> {
    val out = mutableListOf<RelayClaim>()
    for (item in items) {
        val derived = newRelayClaim(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun rankRelayClaim(item: RelayClaim): String = classifyRelayEntry(item.projectedVolume())
