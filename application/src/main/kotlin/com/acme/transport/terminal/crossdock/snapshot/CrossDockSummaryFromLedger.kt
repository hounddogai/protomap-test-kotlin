package com.acme.transport.terminal.crossdock.snapshot

import com.acme.transport.terminal.traileryard.ledger.TrailerYardLedger
import com.acme.transport.terminal.traileryard.ledger.normalizeTrailerYardEntryCode

fun summarizeTrailerYardLedgersForSummary(items: MutableList<TrailerYardLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveCrossDockSummariesFromLedger(items: MutableList<TrailerYardLedger>): MutableList<CrossDockSummary> {
    val out = mutableListOf<CrossDockSummary>()
    for (item in items) {
        val derived = newCrossDockSummary(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun deriveCrossDockSummary(item: CrossDockSummary): String = normalizeTrailerYardEntryCode(item.code)
