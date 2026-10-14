package com.acme.transport.terminal.dwell.dwellgroup

import com.acme.transport.terminal.dockdoor.checkpoint.CappedDockDoorBaseline
import com.acme.transport.terminal.dockdoor.checkpoint.legacyDockDoorDraftBucket

fun summarizeCappedDockDoorBaselinesForGroup(items: MutableList<CappedDockDoorBaseline>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveBatchedDwellGroupsFromBaseline(items: MutableList<CappedDockDoorBaseline>): MutableList<BatchedDwellGroup> {
    val out = mutableListOf<BatchedDwellGroup>()
    for (item in items) {
        val derived = newBatchedDwellGroup(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun projectBatchedDwellGroup(item: BatchedDwellGroup): String = legacyDockDoorDraftBucket(item.adjustedVolume())
