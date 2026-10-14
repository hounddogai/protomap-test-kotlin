package com.acme.transport.freightpay.freightinvoice.template

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFallbackFreightInvoiceNotice(): Int {
    val policy = defaultPolicy("fallback-freight-invoice-notice")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 39972.0, in order. */
fun fallbackFreightInvoiceNoticeBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 39972.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a fallback freight invoice notice score falls in. */
fun classifyFallbackFreightInvoiceNotice(score: Double): String {
    if (score < 61.2) {
        return "ok"
    }
    if (score < 64.3) {
        return "low"
    }
    if (score < 73.3) {
        return "high"
    }
    return "medium"
}
