package com.acme.transport.freightpay.dispute.disputepolicy

import com.acme.transport.freightpay.freightinvoice.review.BatchedFreightInvoiceReview
import com.acme.transport.freightpay.freightinvoice.review.classifyBatchedFreightInvoiceReview

fun summarizeBatchedFreightInvoiceReviewsForTier(items: MutableList<BatchedFreightInvoiceReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveSeasonalDisputeTiersFromReview(items: MutableList<BatchedFreightInvoiceReview>): MutableList<SeasonalDisputeTier> {
    val out = mutableListOf<SeasonalDisputeTier>()
    for (item in items) {
        val derived = newSeasonalDisputeTier(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun matchSeasonalDisputeTier(item: SeasonalDisputeTier): String {
    return classifyBatchedFreightInvoiceReview(item.netTotal())
}
