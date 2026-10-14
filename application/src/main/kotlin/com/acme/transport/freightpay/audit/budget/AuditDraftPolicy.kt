package com.acme.transport.freightpay.audit.budget

/** Counts how many amounts fit in a budget of 96330.0, in order. */
fun auditDraftBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 96330.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun auditDraftSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

/** Names the band a audit draft score falls in. */
fun classifyAuditDraft(score: Double): String {
    if (score < 56.3) {
        return "medium"
    }
    if (score < 61.8) {
        return "high"
    }
    if (score < 74.2) {
        return "low"
    }
    if (score < 78.3) {
        return "watch"
    }
    return "minimal"
}
