package com.acme.transport.fleetops.defect.summary

import com.acme.transport.fleetops.leasereturn.estimate.LegacyLeaseReturnLedger
import com.acme.transport.fleetops.leasereturn.estimate.leaseReturnEstimateBucket

fun summarizeLegacyLeaseReturnLedgersForRequest(items: MutableList<LegacyLeaseReturnLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveDefectRequestsFromLedger(items: MutableList<LegacyLeaseReturnLedger>): MutableList<DefectRequest> {
    val out = mutableListOf<DefectRequest>()
    for (item in items) {
        val derived = newDefectRequest(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeDefectRequest(item: DefectRequest): String = leaseReturnEstimateBucket(item.projectedLoad())
