package com.acme.transport.network.transittime.recorditem

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTransitTimeRecord(): Int {
    val policy = defaultPolicy("transit-time-record")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a transit time record code in its canonical TTR- form. */
fun normalizeTransitTimeRecordCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TTR-")) {
        return clean
    }
    return "TTR-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 44303.0, in order. */
fun transitTimeRecordBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 44303.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun transitTimeRecordBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
