package com.acme.transport.network.zone.recorditem

import com.acme.transport.network.node.draft.NodeDraft
import com.acme.transport.network.node.draft.classifyNodeReservation

fun summarizeNodeDraftsForRecord(items: MutableList<NodeDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveZoneRecordsFromDraft(items: MutableList<NodeDraft>): MutableList<ZoneRecord> {
    val out = mutableListOf<ZoneRecord>()
    for (item in items) {
        val derived = newZoneRecord(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileZoneRecord(item: ZoneRecord): String = classifyNodeReservation(item.projectedVolume())
