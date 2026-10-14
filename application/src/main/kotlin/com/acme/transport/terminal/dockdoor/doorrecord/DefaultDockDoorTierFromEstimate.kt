package com.acme.transport.terminal.dockdoor.doorrecord

import com.acme.transport.terminal.dockdoor.doorestimate.FallbackDockDoorEstimate
import com.acme.transport.terminal.dockdoor.doorestimate.normalizeDefaultDockDoorPreferenceCode

fun summarizeFallbackDockDoorEstimatesForTier(items: MutableList<FallbackDockDoorEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveDefaultDockDoorTiersFromEstimate(items: MutableList<FallbackDockDoorEstimate>): MutableList<DefaultDockDoorTier> {
    val out = mutableListOf<DefaultDockDoorTier>()
    for (item in items) {
        val derived = newDefaultDockDoorTier(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun balanceDefaultDockDoorTier(item: DefaultDockDoorTier): String {
    return normalizeDefaultDockDoorPreferenceCode(item.code)
}
