package com.acme.transport.freightpay.dispute.disputetemplate

import com.acme.transport.freightpay.accessorial.accessorialmetric.ScheduledAccessorialReview
import com.acme.transport.freightpay.accessorial.accessorialmetric.lenientAccessorialMetricWeight

fun summarizeScheduledAccessorialReviewsForSnapshot(items: MutableList<ScheduledAccessorialReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveManualDisputeSnapshotsFromReview(items: MutableList<ScheduledAccessorialReview>): MutableList<ManualDisputeSnapshot> {
    val out = mutableListOf<ManualDisputeSnapshot>()
    for (item in items) {
        val derived = newManualDisputeSnapshot(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveManualDisputeSnapshot(item: ManualDisputeSnapshot): Double = lenientAccessorialMetricWeight(7, true)
