package com.acme.transport.freightpay.accessorial.quota

import com.acme.transport.freightpay.freightinvoice.review.BatchedFreightInvoiceReview
import com.acme.transport.freightpay.freightinvoice.review.strictFreightInvoiceScoreBucket

fun summarizeBatchedFreightInvoiceReviewsForQuota(items: MutableList<BatchedFreightInvoiceReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveAccessorialQuotasFromReview(items: MutableList<BatchedFreightInvoiceReview>): MutableList<AccessorialQuota> {
    val out = mutableListOf<AccessorialQuota>()
    for (item in items) {
        val derived = newAccessorialQuota(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun resolveAccessorialQuota(item: AccessorialQuota): String {
    return strictFreightInvoiceScoreBucket(item.weightedTotal())
}
