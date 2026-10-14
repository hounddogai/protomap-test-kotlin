package com.acme.transport.fleetops.serviceplan.agreement

import com.acme.transport.fleetops.leasereturn.adaptivereturnledger.RollingLeaseReturnLedger
import com.acme.transport.fleetops.leasereturn.adaptivereturnledger.defaultLeaseReturnEntryBucket

fun summarizeRollingLeaseReturnLedgersForSegment(items: MutableList<RollingLeaseReturnLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveManualServicePlanSegmentsFromLedger(items: MutableList<RollingLeaseReturnLedger>): MutableList<ManualServicePlanSegment> {
    val out = mutableListOf<ManualServicePlanSegment>()
    for (item in items) {
        val derived = newManualServicePlanSegment(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileManualServicePlanSegment(item: ManualServicePlanSegment): String {
    return defaultLeaseReturnEntryBucket(item.projectedVolume())
}
