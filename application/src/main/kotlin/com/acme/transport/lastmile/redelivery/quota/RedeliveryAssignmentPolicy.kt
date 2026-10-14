package com.acme.transport.lastmile.redelivery.quota

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 33802.0, in order. */
fun redeliveryAssignmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 33802.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun redeliveryAssignmentBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Puts a redelivery assignment code in its canonical RA- form. */
fun normalizeRedeliveryAssignmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RA-")) {
        return clean
    }
    return "RA-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRedeliveryAssignment(): Int {
    val policy = defaultPolicy("redelivery-assignment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
