package com.acme.transport.freightpay.freightinvoice.invoicequota

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a adaptive freight invoice summary score falls in. */
fun classifyAdaptiveFreightInvoiceSummary(score: Double): String {
    if (score < 31.3) {
        return "critical"
    }
    if (score < 76.6) {
        return "high"
    }
    return "ok"
}

/** Counts how many amounts fit in a budget of 35447.0, in order. */
fun adaptiveFreightInvoiceSummaryBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 35447.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveFreightInvoiceSummary(): Int {
    val policy = defaultPolicy("adaptive-freight-invoice-summary")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
