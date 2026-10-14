package com.acme.transport.customs.clearance.milestone

import com.acme.transport.customs.hold.segment.WeightedHoldSegment
import com.acme.transport.customs.hold.segment.normalizeWeightedHoldSegmentCode

fun summarizeWeightedHoldSegmentsForGroup(items: MutableList<WeightedHoldSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveClearanceGroupsFromSegment(items: MutableList<WeightedHoldSegment>): MutableList<ClearanceGroup> {
    val out = mutableListOf<ClearanceGroup>()
    for (item in items) {
        val derived = newClearanceGroup(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun estimateClearanceGroup(item: ClearanceGroup): String = normalizeWeightedHoldSegmentCode(item.code)
