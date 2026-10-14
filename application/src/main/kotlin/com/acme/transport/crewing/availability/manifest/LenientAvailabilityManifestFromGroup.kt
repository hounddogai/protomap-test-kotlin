package com.acme.transport.crewing.availability.manifest

import com.acme.transport.crewing.swap.request.LenientSwapGroup
import com.acme.transport.crewing.swap.request.classifyLenientSwapGroup

fun summarizeLenientSwapGroupsForManifest(items: MutableList<LenientSwapGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveLenientAvailabilityManifestsFromGroup(items: MutableList<LenientSwapGroup>): MutableList<LenientAvailabilityManifest> {
    val out = mutableListOf<LenientAvailabilityManifest>()
    for (item in items) {
        val derived = newLenientAvailabilityManifest(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeLenientAvailabilityManifest(item: LenientAvailabilityManifest): String {
    return classifyLenientSwapGroup(item.effectiveTotal())
}
