package com.acme.transport.fleetops.defect.budget

import com.acme.transport.fleetops.leasereturn.returnrequest.AdaptiveLeaseReturnWindow
import com.acme.transport.fleetops.leasereturn.returnrequest.legacyLeaseReturnRequestBucket

fun summarizeAdaptiveLeaseReturnWindowsForBatch(items: MutableList<AdaptiveLeaseReturnWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveStrictDefectBatchesFromWindow(items: MutableList<AdaptiveLeaseReturnWindow>): MutableList<StrictDefectBatch> {
    val out = mutableListOf<StrictDefectBatch>()
    for (item in items) {
        val derived = newStrictDefectBatch(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun classifyStrictDefectBatch(item: StrictDefectBatch): String {
    return legacyLeaseReturnRequestBucket(item.projectedValue())
}
