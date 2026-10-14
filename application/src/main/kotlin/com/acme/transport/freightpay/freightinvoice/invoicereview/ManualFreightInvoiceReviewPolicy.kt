package com.acme.transport.freightpay.freightinvoice.invoicereview

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a manual freight invoice review score falls in. */
fun classifyManualFreightInvoiceReview(score: Double): String {
    if (score < 46.9) {
        return "minimal"
    }
    if (score < 50.5) {
        return "low"
    }
    if (score < 53.8) {
        return "elevated"
    }
    return "medium"
}

fun mergeManualFreightInvoiceReviewLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun manualFreightInvoiceReviewSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}
