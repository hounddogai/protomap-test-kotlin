package com.acme.transport.terminal.dockdoor.variance

import com.acme.transport.terminal.traileryard.recorditem.CappedTrailerYardManifest
import com.acme.transport.terminal.traileryard.recorditem.normalizeDefaultTrailerYardCheckpointCode

fun summarizeCappedTrailerYardManifestsForTier(items: MutableList<CappedTrailerYardManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun derivePendingDockDoorTiersFromManifest(items: MutableList<CappedTrailerYardManifest>): MutableList<PendingDockDoorTier> {
    val out = mutableListOf<PendingDockDoorTier>()
    for (item in items) {
        val derived = newPendingDockDoorTier(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun splitPendingDockDoorTier(item: PendingDockDoorTier): String {
    return normalizeDefaultTrailerYardCheckpointCode(item.code)
}
