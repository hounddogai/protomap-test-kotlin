package com.acme.transport.fleetops.tire.manifest

import com.acme.transport.fleetops.leasereturn.recorditem.RegionalLeaseReturnRecord
import com.acme.transport.fleetops.leasereturn.recorditem.classifyLenientLeaseReturnLedger

fun summarizeRegionalLeaseReturnRecordsForRequest(items: MutableList<RegionalLeaseReturnRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveSeasonalTireRequestsFromRecord(items: MutableList<RegionalLeaseReturnRecord>): MutableList<SeasonalTireRequest> {
    val out = mutableListOf<SeasonalTireRequest>()
    for (item in items) {
        val derived = newSeasonalTireRequest(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun deriveSeasonalTireRequest(item: SeasonalTireRequest): String {
    return classifyLenientLeaseReturnLedger(item.netLoad())
}
