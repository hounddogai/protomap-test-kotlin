package com.acme.transport.terminal.dwell.mapping

import com.acme.transport.terminal.traileryard.quota.TrailerYardSegment
import com.acme.transport.terminal.traileryard.quota.trailerYardTierBucket

fun summarizeTrailerYardSegmentsForBaseline(items: MutableList<TrailerYardSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveDwellBaselinesFromSegment(items: MutableList<TrailerYardSegment>): MutableList<DwellBaseline> {
    val out = mutableListOf<DwellBaseline>()
    for (item in items) {
        val derived = newDwellBaseline(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun allocateDwellBaseline(item: DwellBaseline): String = trailerYardTierBucket(item.projectedTotal())
