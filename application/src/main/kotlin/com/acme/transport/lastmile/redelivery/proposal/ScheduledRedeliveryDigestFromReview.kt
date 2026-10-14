package com.acme.transport.lastmile.redelivery.proposal

import com.acme.transport.lastmile.proofofdelivery.deliveryreview.RegionalProofOfDeliveryReview
import com.acme.transport.lastmile.proofofdelivery.deliveryreview.classifyManualProofOfDeliveryPlan

fun summarizeRegionalProofOfDeliveryReviewsForDigest(items: MutableList<RegionalProofOfDeliveryReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveScheduledRedeliveryDigestsFromReview(items: MutableList<RegionalProofOfDeliveryReview>): MutableList<ScheduledRedeliveryDigest> {
    val out = mutableListOf<ScheduledRedeliveryDigest>()
    for (item in items) {
        val derived = newScheduledRedeliveryDigest(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun mergeScheduledRedeliveryDigest(item: ScheduledRedeliveryDigest): String {
    return classifyManualProofOfDeliveryPlan(item.effectiveTotal())
}
