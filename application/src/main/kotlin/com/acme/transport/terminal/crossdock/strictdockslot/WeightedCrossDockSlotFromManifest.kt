package com.acme.transport.terminal.crossdock.strictdockslot

import com.acme.transport.terminal.traileryard.claim.RegionalTrailerYardManifest
import com.acme.transport.terminal.traileryard.claim.normalizeRegionalTrailerYardManifestCode

fun summarizeRegionalTrailerYardManifestsForSlot(items: MutableList<RegionalTrailerYardManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveWeightedCrossDockSlotsFromManifest(items: MutableList<RegionalTrailerYardManifest>): MutableList<WeightedCrossDockSlot> {
    val out = mutableListOf<WeightedCrossDockSlot>()
    for (item in items) {
        val derived = newWeightedCrossDockSlot(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scoreWeightedCrossDockSlot(item: WeightedCrossDockSlot): String {
    return normalizeRegionalTrailerYardManifestCode(item.code)
}
