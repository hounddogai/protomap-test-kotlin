package com.acme.transport.crewing.qualification.allocation

import com.acme.transport.crewing.swap.snapshot.SwapRequest
import com.acme.transport.crewing.swap.snapshot.classifyPendingSwapSnapshot

fun summarizeSwapRequestsForSnapshot(items: MutableList<SwapRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveQualificationSnapshotsFromRequest(items: MutableList<SwapRequest>): MutableList<QualificationSnapshot> {
    val out = mutableListOf<QualificationSnapshot>()
    for (item in items) {
        val derived = newQualificationSnapshot(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun planQualificationSnapshot(item: QualificationSnapshot): String {
    return classifyPendingSwapSnapshot(item.weightedValue())
}
