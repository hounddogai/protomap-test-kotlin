package com.acme.transport.freightpay.freightinvoice.summary

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCappedFreightInvoicePlan(): Int {
    val policy = defaultPolicy("capped-freight-invoice-plan")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeCappedFreightInvoicePlanLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a capped freight invoice plan score falls in. */
fun classifyCappedFreightInvoicePlan(score: Double): String {
    if (score < 10.1) {
        return "watch"
    }
    if (score < 58.1) {
        return "medium"
    }
    if (score < 91.5) {
        return "elevated"
    }
    if (score < 94.9) {
        return "low"
    }
    return "critical"
}

/** Counts how many amounts fit in a budget of 81314.0, in order. */
fun cappedFreightInvoicePlanBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 81314.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
