package com.acme.transport.network.node.snapshot

import com.acme.transport.network.node.forecast.NodeLedger
import com.acme.transport.network.node.forecast.normalizeNodeLedgerCode

fun summarizeNodeLedgersForSnapshot(items: MutableList<NodeLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveLenientNodeSnapshotsFromLedger(items: MutableList<NodeLedger>): MutableList<LenientNodeSnapshot> {
    val out = mutableListOf<LenientNodeSnapshot>()
    for (item in items) {
        val derived = newLenientNodeSnapshot(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankLenientNodeSnapshot(item: LenientNodeSnapshot): String = normalizeNodeLedgerCode(item.code)
