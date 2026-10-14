package com.acme.transport.network.transittime.notice

import com.acme.transport.network.servicearea.batch.PendingServiceAreaAssignment
import com.acme.transport.network.servicearea.batch.tieredServiceAreaBatchBucket

fun summarizePendingServiceAreaAssignmentsForNotice(items: MutableList<PendingServiceAreaAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveTieredTransitTimeNoticesFromAssignment(items: MutableList<PendingServiceAreaAssignment>): MutableList<TieredTransitTimeNotice> {
    val out = mutableListOf<TieredTransitTimeNotice>()
    for (item in items) {
        val derived = newTieredTransitTimeNotice(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun computeTieredTransitTimeNotice(item: TieredTransitTimeNotice): String {
    return tieredServiceAreaBatchBucket(item.measuredValue())
}
