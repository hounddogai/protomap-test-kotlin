package com.acme.transport.terminal.sortation.sortationadjustment

import com.acme.transport.terminal.traileryard.claim.RegionalTrailerYardManifest
import com.acme.transport.terminal.traileryard.claim.normalizeRegionalTrailerYardManifestCode

fun summarizeRegionalTrailerYardManifestsForAdjustment(items: MutableList<RegionalTrailerYardManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveScheduledSortationAdjustmentsFromManifest(items: MutableList<RegionalTrailerYardManifest>): MutableList<ScheduledSortationAdjustment> {
    val out = mutableListOf<ScheduledSortationAdjustment>()
    for (item in items) {
        val derived = newScheduledSortationAdjustment(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun estimateScheduledSortationAdjustment(item: ScheduledSortationAdjustment): String {
    return normalizeRegionalTrailerYardManifestCode(item.code)
}
