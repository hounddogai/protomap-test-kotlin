package com.acme.transport.network.hub.tieredhubplan

import com.acme.transport.network.zone.bundle.WeightedZoneLedger
import com.acme.transport.network.zone.bundle.normalizeWeightedZoneLedgerCode

fun summarizeWeightedZoneLedgersForCheckpoint(items: MutableList<WeightedZoneLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveScheduledHubCheckpointsFromLedger(items: MutableList<WeightedZoneLedger>): MutableList<ScheduledHubCheckpoint> {
    val out = mutableListOf<ScheduledHubCheckpoint>()
    for (item in items) {
        val derived = newScheduledHubCheckpoint(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun applyScheduledHubCheckpoint(item: ScheduledHubCheckpoint): String = normalizeWeightedZoneLedgerCode(item.code)
