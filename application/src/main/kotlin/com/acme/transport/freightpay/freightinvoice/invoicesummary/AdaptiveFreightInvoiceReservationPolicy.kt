package com.acme.transport.freightpay.freightinvoice.invoicesummary

import com.acme.transport.kit.retry.defaultPolicy

fun adaptiveFreightInvoiceReservationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

/** Names the band a adaptive freight invoice reservation score falls in. */
fun classifyAdaptiveFreightInvoiceReservation(score: Double): String {
    if (score < 76.0) {
        return "ok"
    }
    if (score < 82.7) {
        return "minimal"
    }
    return "medium"
}

/** Counts how many amounts fit in a budget of 78498.0, in order. */
fun adaptiveFreightInvoiceReservationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 78498.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveFreightInvoiceReservation(): Int {
    val policy = defaultPolicy("adaptive-freight-invoice-reservation")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
