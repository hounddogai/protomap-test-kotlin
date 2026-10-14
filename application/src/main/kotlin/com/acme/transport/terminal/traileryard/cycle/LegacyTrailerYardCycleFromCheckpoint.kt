package com.acme.transport.terminal.traileryard.cycle

import com.acme.transport.terminal.crossdock.ledger.LegacyCrossDockCheckpoint
import com.acme.transport.terminal.crossdock.ledger.lenientCrossDockQuotaBucket

fun summarizeLegacyCrossDockCheckpointsForCycle(items: MutableList<LegacyCrossDockCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveLegacyTrailerYardCyclesFromCheckpoint(items: MutableList<LegacyCrossDockCheckpoint>): MutableList<LegacyTrailerYardCycle> {
    val out = mutableListOf<LegacyTrailerYardCycle>()
    for (item in items) {
        val derived = newLegacyTrailerYardCycle(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun splitLegacyTrailerYardCycle(item: LegacyTrailerYardCycle): String {
    return lenientCrossDockQuotaBucket(item.netVolume())
}
