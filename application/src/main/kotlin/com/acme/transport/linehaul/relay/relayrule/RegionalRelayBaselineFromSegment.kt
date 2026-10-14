package com.acme.transport.linehaul.relay.relayrule

import com.acme.transport.linehaul.trailer.segment.PendingTrailerSegment
import com.acme.transport.linehaul.trailer.segment.normalizeManualTrailerOverrideCode

fun summarizePendingTrailerSegmentsForBaseline(items: MutableList<PendingTrailerSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveRegionalRelayBaselinesFromSegment(items: MutableList<PendingTrailerSegment>): MutableList<RegionalRelayBaseline> {
    val out = mutableListOf<RegionalRelayBaseline>()
    for (item in items) {
        val derived = newRegionalRelayBaseline(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveRegionalRelayBaseline(item: RegionalRelayBaseline): String {
    return normalizeManualTrailerOverrideCode(item.code)
}
