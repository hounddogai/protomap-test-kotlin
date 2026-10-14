package com.acme.transport.freightpay.freightinvoice.invoicemanifest

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun weightedFreightInvoicePolicyBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Counts how many amounts fit in a budget of 96163.0, in order. */
fun weightedFreightInvoicePolicyBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 96163.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeWeightedFreightInvoicePolicy(): Int {
    val policy = defaultPolicy("weighted-freight-invoice-policy")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a weighted freight invoice policy score falls in. */
fun classifyWeightedFreightInvoicePolicy(score: Double): String {
    if (score < 16.7) {
        return "critical"
    }
    if (score < 33.6) {
        return "low"
    }
    if (score < 65.7) {
        return "medium"
    }
    return "minimal"
}
