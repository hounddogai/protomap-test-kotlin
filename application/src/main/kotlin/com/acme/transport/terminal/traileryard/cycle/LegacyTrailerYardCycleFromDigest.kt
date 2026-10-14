package com.acme.transport.terminal.traileryard.cycle

import com.acme.transport.terminal.sortation.sortationdigest.PendingSortationDigest
import com.acme.transport.terminal.sortation.sortationdigest.pendingSortationDigestBucket

fun summarizePendingSortationDigestsForCycle(items: MutableList<PendingSortationDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveLegacyTrailerYardCyclesFromDigest(items: MutableList<PendingSortationDigest>): MutableList<LegacyTrailerYardCycle> {
    val out = mutableListOf<LegacyTrailerYardCycle>()
    for (item in items) {
        val derived = newLegacyTrailerYardCycle(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun applyLegacyTrailerYardCycle(item: LegacyTrailerYardCycle): String {
    return pendingSortationDigestBucket(item.netVolume())
}
