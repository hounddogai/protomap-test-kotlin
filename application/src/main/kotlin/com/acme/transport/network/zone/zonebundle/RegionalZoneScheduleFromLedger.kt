package com.acme.transport.network.zone.zonebundle

import com.acme.transport.network.node.nodedraft.BatchedNodeLedger
import com.acme.transport.network.node.nodedraft.normalizeRollingNodeDraftCode

fun summarizeBatchedNodeLedgersForSchedule(items: MutableList<BatchedNodeLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveRegionalZoneSchedulesFromLedger(items: MutableList<BatchedNodeLedger>): MutableList<RegionalZoneSchedule> {
    val out = mutableListOf<RegionalZoneSchedule>()
    for (item in items) {
        val derived = newRegionalZoneSchedule(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun applyRegionalZoneSchedule(item: RegionalZoneSchedule): String = normalizeRollingNodeDraftCode(item.code)
