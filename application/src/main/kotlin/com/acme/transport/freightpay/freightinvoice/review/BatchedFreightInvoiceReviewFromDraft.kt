package com.acme.transport.freightpay.freightinvoice.review

import com.acme.transport.freightpay.ratecard.regionalcardsnapshot.PrimaryRateCardDraft
import com.acme.transport.freightpay.ratecard.regionalcardsnapshot.normalizeGlobalRateCardAssignmentCode

fun summarizePrimaryRateCardDraftsForReview(items: MutableList<PrimaryRateCardDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveBatchedFreightInvoiceReviewsFromDraft(items: MutableList<PrimaryRateCardDraft>): MutableList<BatchedFreightInvoiceReview> {
    val out = mutableListOf<BatchedFreightInvoiceReview>()
    for (item in items) {
        val derived = newBatchedFreightInvoiceReview(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeBatchedFreightInvoiceReview(item: BatchedFreightInvoiceReview): String {
    return normalizeGlobalRateCardAssignmentCode(item.code)
}
