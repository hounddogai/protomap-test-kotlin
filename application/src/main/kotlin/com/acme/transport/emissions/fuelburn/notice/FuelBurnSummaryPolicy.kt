package com.acme.transport.emissions.fuelburn.notice

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFuelBurnSummary(): Int {
    val policy = defaultPolicy("fuel-burn-summary")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun fuelBurnSummarySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

/** Puts a fuel burn summary code in its canonical FBS- form. */
fun normalizeFuelBurnSummaryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FBS-")) {
        return clean
    }
    return "FBS-${clean.replace(" ", "-")}"
}

fun fuelBurnSummaryBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
