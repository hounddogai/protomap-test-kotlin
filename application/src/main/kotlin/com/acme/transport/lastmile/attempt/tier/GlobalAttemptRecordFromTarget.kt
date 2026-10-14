package com.acme.transport.lastmile.attempt.tier

import com.acme.transport.lastmile.drop.recorditem.DropTarget
import com.acme.transport.lastmile.drop.recorditem.classifyGlobalDropException

fun summarizeDropTargetsForRecord(items: MutableList<DropTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveGlobalAttemptRecordsFromTarget(items: MutableList<DropTarget>): MutableList<GlobalAttemptRecord> {
    val out = mutableListOf<GlobalAttemptRecord>()
    for (item in items) {
        val derived = newGlobalAttemptRecord(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun classifyGlobalAttemptRecordExtra(item: GlobalAttemptRecord): String {
    return classifyGlobalDropException(item.measuredValue())
}
