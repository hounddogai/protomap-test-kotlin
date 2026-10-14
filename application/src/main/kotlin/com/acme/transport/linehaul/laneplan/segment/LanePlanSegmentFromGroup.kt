package com.acme.transport.linehaul.laneplan.segment

import com.acme.transport.linehaul.trailer.group.StrictTrailerGroup
import com.acme.transport.linehaul.trailer.group.trailerMappingBucket

fun summarizeStrictTrailerGroupsForSegment(items: MutableList<StrictTrailerGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveLanePlanSegmentsFromGroup(items: MutableList<StrictTrailerGroup>): MutableList<LanePlanSegment> {
    val out = mutableListOf<LanePlanSegment>()
    for (item in items) {
        val derived = newLanePlanSegment(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun planLanePlanSegment(item: LanePlanSegment): String = trailerMappingBucket(item.effectiveLoad())
