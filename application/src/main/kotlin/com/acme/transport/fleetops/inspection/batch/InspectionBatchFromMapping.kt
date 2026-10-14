package com.acme.transport.fleetops.inspection.batch

import com.acme.transport.fleetops.leasereturn.rule.LeaseReturnMapping
import com.acme.transport.fleetops.leasereturn.rule.normalizeLeaseReturnSnapshotCode

fun summarizeLeaseReturnMappingsForBatch(items: MutableList<LeaseReturnMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveInspectionBatchesFromMapping(items: MutableList<LeaseReturnMapping>): MutableList<InspectionBatch> {
    val out = mutableListOf<InspectionBatch>()
    for (item in items) {
        val derived = newInspectionBatch(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveInspectionBatch(item: InspectionBatch): String = normalizeLeaseReturnSnapshotCode(item.code)
