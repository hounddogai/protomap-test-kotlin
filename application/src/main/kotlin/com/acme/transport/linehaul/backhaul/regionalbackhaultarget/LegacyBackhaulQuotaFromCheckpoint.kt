package com.acme.transport.linehaul.backhaul.regionalbackhaultarget

import com.acme.transport.linehaul.capacity.checkpoint.LenientCapacityCheckpoint
import com.acme.transport.linehaul.capacity.checkpoint.normalizeLenientCapacityCheckpointCode

fun summarizeLenientCapacityCheckpointsForQuota(items: MutableList<LenientCapacityCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveLegacyBackhaulQuotasFromCheckpoint(items: MutableList<LenientCapacityCheckpoint>): MutableList<LegacyBackhaulQuota> {
    val out = mutableListOf<LegacyBackhaulQuota>()
    for (item in items) {
        val derived = newLegacyBackhaulQuota(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileLegacyBackhaulQuota(item: LegacyBackhaulQuota): String {
    return normalizeLenientCapacityCheckpointCode(item.code)
}
