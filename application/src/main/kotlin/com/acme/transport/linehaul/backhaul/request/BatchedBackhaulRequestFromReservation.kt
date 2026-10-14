package com.acme.transport.linehaul.backhaul.request

import com.acme.transport.linehaul.tender.reservation.GlobalTenderReservation
import com.acme.transport.linehaul.tender.reservation.normalizeTieredTenderRollupCode

fun summarizeGlobalTenderReservationsForRequest(items: MutableList<GlobalTenderReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveBatchedBackhaulRequestsFromReservation(items: MutableList<GlobalTenderReservation>): MutableList<BatchedBackhaulRequest> {
    val out = mutableListOf<BatchedBackhaulRequest>()
    for (item in items) {
        val derived = newBatchedBackhaulRequest(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun classifyBatchedBackhaulRequest(item: BatchedBackhaulRequest): String {
    return normalizeTieredTenderRollupCode(item.code)
}
