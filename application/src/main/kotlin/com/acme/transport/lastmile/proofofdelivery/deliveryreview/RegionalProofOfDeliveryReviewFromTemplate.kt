package com.acme.transport.lastmile.proofofdelivery.deliveryreview

import com.acme.transport.lastmile.drop.forecast.TieredDropTemplate
import com.acme.transport.lastmile.drop.forecast.seasonalDropScheduleWeight

fun summarizeTieredDropTemplatesForReview(items: MutableList<TieredDropTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveRegionalProofOfDeliveryReviewsFromTemplate(items: MutableList<TieredDropTemplate>): MutableList<RegionalProofOfDeliveryReview> {
    val out = mutableListOf<RegionalProofOfDeliveryReview>()
    for (item in items) {
        val derived = newRegionalProofOfDeliveryReview(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileRegionalProofOfDeliveryReview(item: RegionalProofOfDeliveryReview): Double {
    return seasonalDropScheduleWeight(2, true)
}
