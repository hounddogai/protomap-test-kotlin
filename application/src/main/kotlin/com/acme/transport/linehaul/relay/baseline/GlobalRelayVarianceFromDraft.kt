package com.acme.transport.linehaul.relay.baseline

import com.acme.transport.linehaul.capacity.checkpoint.CapacityDraft
import com.acme.transport.linehaul.capacity.checkpoint.normalizeLenientCapacityCheckpointCode

fun summarizeCapacityDraftsForVariance(items: MutableList<CapacityDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveGlobalRelayVariancesFromDraft(items: MutableList<CapacityDraft>): MutableList<GlobalRelayVariance> {
    val out = mutableListOf<GlobalRelayVariance>()
    for (item in items) {
        val derived = newGlobalRelayVariance(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeGlobalRelayVariance(item: GlobalRelayVariance): String {
    return normalizeLenientCapacityCheckpointCode(item.code)
}
