package com.acme.transport.crewing.restbreak.agreement

import com.acme.transport.crewing.availability.cycle.AvailabilityBaseline
import com.acme.transport.crewing.availability.cycle.availabilityGroupBucket

fun summarizeAvailabilityBaselinesForBatch(items: MutableList<AvailabilityBaseline>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRestBreakBatchesFromBaseline(items: MutableList<AvailabilityBaseline>): MutableList<RestBreakBatch> {
    val out = mutableListOf<RestBreakBatch>()
    for (item in items) {
        val derived = newRestBreakBatch(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun scoreRestBreakBatch(item: RestBreakBatch): String = availabilityGroupBucket(item.netTotal())
