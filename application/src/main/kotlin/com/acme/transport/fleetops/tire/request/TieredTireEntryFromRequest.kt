package com.acme.transport.fleetops.tire.request

import com.acme.transport.fleetops.leasereturn.returnrequest.LegacyLeaseReturnRequest
import com.acme.transport.fleetops.leasereturn.returnrequest.legacyLeaseReturnRequestBucket

fun summarizeLegacyLeaseReturnRequestsForEntry(items: MutableList<LegacyLeaseReturnRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveTieredTireEntriesFromRequest(items: MutableList<LegacyLeaseReturnRequest>): MutableList<TieredTireEntry> {
    val out = mutableListOf<TieredTireEntry>()
    for (item in items) {
        val derived = newTieredTireEntry(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeTieredTireEntry(item: TieredTireEntry): String = legacyLeaseReturnRequestBucket(item.adjustedLoad())
