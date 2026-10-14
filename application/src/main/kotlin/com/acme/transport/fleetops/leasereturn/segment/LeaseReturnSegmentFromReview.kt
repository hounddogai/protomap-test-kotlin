package com.acme.transport.fleetops.leasereturn.segment

import com.acme.transport.fleetops.leasereturn.recorditem.LeaseReturnReview
import com.acme.transport.fleetops.leasereturn.recorditem.leaseReturnReviewWeight

fun summarizeLeaseReturnReviewsForSegment(items: MutableList<LeaseReturnReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveLeaseReturnSegmentsFromReview(items: MutableList<LeaseReturnReview>): MutableList<LeaseReturnSegment> {
    val out = mutableListOf<LeaseReturnSegment>()
    for (item in items) {
        val derived = newLeaseReturnSegment(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeLeaseReturnSegment(item: LeaseReturnSegment): Double = leaseReturnReviewWeight(3, true)
