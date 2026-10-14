package com.acme.transport.freightpay.freightinvoice.review

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun batchedFreightInvoiceReviewWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.89
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a batched freight invoice review score falls in. */
fun classifyBatchedFreightInvoiceReview(score: Double): String {
    if (score < 4.9) {
        return "medium"
    }
    if (score < 86.3) {
        return "watch"
    }
    return "minimal"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBatchedFreightInvoiceReview(): Int {
    val policy = defaultPolicy("batched-freight-invoice-review")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 10603.0, in order. */
fun batchedFreightInvoiceReviewBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 10603.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
