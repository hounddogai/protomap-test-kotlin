package com.acme.transport.crewing.restbreak.segment

import com.acme.transport.crewing.availability.manifest.ManualAvailabilityRequest
import com.acme.transport.crewing.availability.manifest.classifyManualAvailabilityRequest

fun summarizeManualAvailabilityRequestsForSegment(items: MutableList<ManualAvailabilityRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveGlobalRestBreakSegmentsFromRequest(items: MutableList<ManualAvailabilityRequest>): MutableList<GlobalRestBreakSegment> {
    val out = mutableListOf<GlobalRestBreakSegment>()
    for (item in items) {
        val derived = newGlobalRestBreakSegment(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun resolveGlobalRestBreakSegment(item: GlobalRestBreakSegment): String {
    return classifyManualAvailabilityRequest(item.effectiveTotal())
}
