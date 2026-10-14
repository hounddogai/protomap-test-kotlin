package com.acme.transport.freightpay.dispute.disputeforecast

import com.acme.transport.freightpay.accessorial.scheduledaccessorialplan.SeasonalAccessorialAllocation
import com.acme.transport.freightpay.accessorial.scheduledaccessorialplan.seasonalAccessorialPlanWeight

fun summarizeSeasonalAccessorialAllocationsForDigest(items: MutableList<SeasonalAccessorialAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun derivePendingDisputeDigestsFromAllocation(items: MutableList<SeasonalAccessorialAllocation>): MutableList<PendingDisputeDigest> {
    val out = mutableListOf<PendingDisputeDigest>()
    for (item in items) {
        val derived = newPendingDisputeDigest(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun derivePendingDisputeDigest(item: PendingDisputeDigest): Double = seasonalAccessorialPlanWeight(8, true)
