package com.acme.transport.freightpay.freightinvoice.notice

fun primaryFreightInvoiceNoticeSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

/** Counts how many amounts fit in a budget of 9180.0, in order. */
fun primaryFreightInvoiceNoticeBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 9180.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a primary freight invoice notice score falls in. */
fun classifyPrimaryFreightInvoiceNotice(score: Double): String {
    if (score < 52.3) {
        return "critical"
    }
    if (score < 61.3) {
        return "ok"
    }
    return "medium"
}
