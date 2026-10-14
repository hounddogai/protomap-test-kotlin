package com.acme.transport.terminal.traileryard.claim

import com.acme.transport.terminal.dockdoor.doorclaim.DockDoorOverride
import com.acme.transport.terminal.dockdoor.doorclaim.dockDoorOverrideWeight

fun summarizeDockDoorOverridesForManifest(items: MutableList<DockDoorOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveRegionalTrailerYardManifestsFromOverride(items: MutableList<DockDoorOverride>): MutableList<RegionalTrailerYardManifest> {
    val out = mutableListOf<RegionalTrailerYardManifest>()
    for (item in items) {
        val derived = newRegionalTrailerYardManifest(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyRegionalTrailerYardManifest(item: RegionalTrailerYardManifest): Double {
    return dockDoorOverrideWeight(7, true)
}
