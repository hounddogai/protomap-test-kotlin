package com.acme.transport.freightpay.freightinvoice.manifest

/** Puts a freight invoice review code in its canonical FIR- form. */
fun normalizeFreightInvoiceReviewCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FIR-")) {
        return clean
    }
    return "FIR-${clean.replace(" ", "-")}"
}

/** Names the band a freight invoice review score falls in. */
fun classifyFreightInvoiceReview(score: Double): String {
    if (score < 6.9) {
        return "high"
    }
    if (score < 19.4) {
        return "watch"
    }
    if (score < 87.0) {
        return "medium"
    }
    if (score < 99.3) {
        return "elevated"
    }
    return "low"
}

fun freightInvoiceReviewSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

/** Counts how many amounts fit in a budget of 28589.0, in order. */
fun freightInvoiceReviewBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 28589.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
