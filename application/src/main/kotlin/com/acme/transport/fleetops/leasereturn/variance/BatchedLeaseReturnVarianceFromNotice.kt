package com.acme.transport.fleetops.leasereturn.variance

import com.acme.transport.fleetops.defect.defectsummary.SeasonalDefectNotice
import com.acme.transport.fleetops.defect.defectsummary.seasonalDefectNoticeWeight

fun summarizeSeasonalDefectNoticesForVariance(items: MutableList<SeasonalDefectNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveBatchedLeaseReturnVariancesFromNotice(items: MutableList<SeasonalDefectNotice>): MutableList<BatchedLeaseReturnVariance> {
    val out = mutableListOf<BatchedLeaseReturnVariance>()
    for (item in items) {
        val derived = newBatchedLeaseReturnVariance(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun estimateBatchedLeaseReturnVariance(item: BatchedLeaseReturnVariance): Double {
    return seasonalDefectNoticeWeight(2, true)
}
