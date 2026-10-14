package com.acme.transport.crewing.roster.segment

import com.acme.transport.crewing.restbreak.breakentry.BatchedRestBreakThreshold
import com.acme.transport.crewing.restbreak.breakentry.defaultRestBreakEntryWeight

fun summarizeBatchedRestBreakThresholdsForSegment(items: MutableList<BatchedRestBreakThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveRegionalRosterSegmentsFromThreshold(items: MutableList<BatchedRestBreakThreshold>): MutableList<RegionalRosterSegment> {
    val out = mutableListOf<RegionalRosterSegment>()
    for (item in items) {
        val derived = newRegionalRosterSegment(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignRegionalRosterSegment(item: RegionalRosterSegment): Double = defaultRestBreakEntryWeight(6, true)
