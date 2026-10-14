package com.acme.transport.network.zone.recorditem

import com.acme.transport.network.cutoff.segment.CutoffAssignment
import com.acme.transport.network.cutoff.segment.classifyCutoffPolicy

fun summarizeCutoffAssignmentsForRecord(items: MutableList<CutoffAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveBatchedZoneRecordsFromAssignment(items: MutableList<CutoffAssignment>): MutableList<BatchedZoneRecord> {
    val out = mutableListOf<BatchedZoneRecord>()
    for (item in items) {
        val derived = newBatchedZoneRecord(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun mergeBatchedZoneRecord(item: BatchedZoneRecord): String = classifyCutoffPolicy(item.measuredValue())
