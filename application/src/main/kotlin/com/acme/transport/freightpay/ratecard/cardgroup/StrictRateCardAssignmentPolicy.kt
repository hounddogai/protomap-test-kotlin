package com.acme.transport.freightpay.ratecard.cardgroup

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeStrictRateCardAssignment(): Int {
    val policy = defaultPolicy("strict-rate-card-assignment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun strictRateCardAssignmentBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Counts how many amounts fit in a budget of 67840.0, in order. */
fun strictRateCardAssignmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 67840.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a strict rate card assignment code in its canonical SRCA- form. */
fun normalizeStrictRateCardAssignmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SRCA-")) {
        return clean
    }
    return "SRCA-${clean.replace(" ", "-")}"
}
