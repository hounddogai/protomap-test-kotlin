package com.acme.transport.terminal.traileryard.yardentry

import com.acme.transport.terminal.dwell.dwelldigest.ManualDwellManifest
import com.acme.transport.terminal.dwell.dwelldigest.scheduledDwellAdjustmentWeight

fun summarizeManualDwellManifestsForSegment(items: MutableList<ManualDwellManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveLenientTrailerYardSegmentsFromManifest(items: MutableList<ManualDwellManifest>): MutableList<LenientTrailerYardSegment> {
    val out = mutableListOf<LenientTrailerYardSegment>()
    for (item in items) {
        val derived = newLenientTrailerYardSegment(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun allocateLenientTrailerYardSegment(item: LenientTrailerYardSegment): Double {
    return scheduledDwellAdjustmentWeight(8, true)
}
