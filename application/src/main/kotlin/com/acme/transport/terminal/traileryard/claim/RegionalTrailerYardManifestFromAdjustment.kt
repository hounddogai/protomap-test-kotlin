package com.acme.transport.terminal.traileryard.claim

import com.acme.transport.terminal.crossdock.docknotice.AdaptiveCrossDockAdjustment
import com.acme.transport.terminal.crossdock.docknotice.classifyAdaptiveCrossDockNotice

fun summarizeAdaptiveCrossDockAdjustmentsForManifest(items: MutableList<AdaptiveCrossDockAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveRegionalTrailerYardManifestsFromAdjustment(items: MutableList<AdaptiveCrossDockAdjustment>): MutableList<RegionalTrailerYardManifest> {
    val out = mutableListOf<RegionalTrailerYardManifest>()
    for (item in items) {
        val derived = newRegionalTrailerYardManifest(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleRegionalTrailerYardManifest(item: RegionalTrailerYardManifest): String {
    return classifyAdaptiveCrossDockNotice(item.measuredValue())
}
