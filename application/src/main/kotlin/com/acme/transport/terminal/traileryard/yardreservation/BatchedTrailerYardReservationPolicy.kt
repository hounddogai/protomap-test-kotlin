package com.acme.transport.terminal.traileryard.yardreservation

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a batched trailer yard reservation code in its canonical BTYR- form. */
fun normalizeBatchedTrailerYardReservationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BTYR-")) {
        return clean
    }
    return "BTYR-${clean.replace(" ", "-")}"
}

fun batchedTrailerYardReservationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBatchedTrailerYardReservation(): Int {
    val policy = defaultPolicy("batched-trailer-yard-reservation")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 6326.0, in order. */
fun batchedTrailerYardReservationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 6326.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
