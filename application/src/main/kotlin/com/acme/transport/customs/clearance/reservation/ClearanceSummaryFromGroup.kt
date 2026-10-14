package com.acme.transport.customs.clearance.reservation

import com.acme.transport.customs.hold.recorditem.LenientHoldGroup
import com.acme.transport.customs.hold.recorditem.holdRecordBucket

fun summarizeLenientHoldGroupsForSummary(items: MutableList<LenientHoldGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveClearanceSummariesFromGroup(items: MutableList<LenientHoldGroup>): MutableList<ClearanceSummary> {
    val out = mutableListOf<ClearanceSummary>()
    for (item in items) {
        val derived = newClearanceSummary(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankClearanceSummary(item: ClearanceSummary): String = holdRecordBucket(item.adjustedVolume())
