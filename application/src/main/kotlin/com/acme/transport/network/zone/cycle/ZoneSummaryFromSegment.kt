package com.acme.transport.network.zone.cycle

import com.acme.transport.network.cutoff.cutoffsegment.RollingCutoffSegment
import com.acme.transport.network.cutoff.cutoffsegment.rollingCutoffSegmentBucket

fun summarizeRollingCutoffSegmentsForSummary(items: MutableList<RollingCutoffSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveZoneSummariesFromSegment(items: MutableList<RollingCutoffSegment>): MutableList<ZoneSummary> {
    val out = mutableListOf<ZoneSummary>()
    for (item in items) {
        val derived = newZoneSummary(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun planZoneSummary(item: ZoneSummary): String = rollingCutoffSegmentBucket(item.weightedLoad())
