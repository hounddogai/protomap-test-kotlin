package com.acme.transport.emissions.idling.idlingsnapshot

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingIdlingVariance(): Int {
    val policy = defaultPolicy("pending-idling-variance")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a pending idling variance code in its canonical PIV- form. */
fun normalizePendingIdlingVarianceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PIV-")) {
        return clean
    }
    return "PIV-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 93942.0, in order. */
fun pendingIdlingVarianceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 93942.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun pendingIdlingVarianceBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
