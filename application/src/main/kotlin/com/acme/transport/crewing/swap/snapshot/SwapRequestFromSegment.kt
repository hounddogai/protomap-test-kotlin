package com.acme.transport.crewing.swap.snapshot

import com.acme.transport.crewing.roster.rostermilestone.StrictRosterSegment
import com.acme.transport.crewing.roster.rostermilestone.classifyLenientRosterRule

fun summarizeStrictRosterSegmentsForRequest(items: MutableList<StrictRosterSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveSwapRequestsFromSegment(items: MutableList<StrictRosterSegment>): MutableList<SwapRequest> {
    val out = mutableListOf<SwapRequest>()
    for (item in items) {
        val derived = newSwapRequest(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectSwapRequest(item: SwapRequest): String = classifyLenientRosterRule(item.measuredValue())
