package com.acme.transport.terminal.traileryard.yardentry

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Puts a lenient trailer yard segment code in its canonical LTYS- form. */
fun normalizeLenientTrailerYardSegmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LTYS-")) {
        return clean
    }
    return "LTYS-${clean.replace(" ", "-")}"
}

fun lenientTrailerYardSegmentBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Counts how many amounts fit in a budget of 30839.0, in order. */
fun lenientTrailerYardSegmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 30839.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientTrailerYardSegment(): Int {
    val policy = defaultPolicy("lenient-trailer-yard-segment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
