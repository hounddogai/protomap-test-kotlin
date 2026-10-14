package com.acme.transport.emissions.evcharge.recorditem

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryEvChargeDigest(): Int {
    val policy = defaultPolicy("primary-ev-charge-digest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun primaryEvChargeDigestBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Puts a primary ev charge digest code in its canonical PECD- form. */
fun normalizePrimaryEvChargeDigestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PECD-")) {
        return clean
    }
    return "PECD-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 79102.0, in order. */
fun primaryEvChargeDigestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 79102.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
