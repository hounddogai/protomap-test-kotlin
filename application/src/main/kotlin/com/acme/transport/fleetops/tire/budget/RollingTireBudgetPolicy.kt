package com.acme.transport.fleetops.tire.budget

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Puts a rolling tire budget code in its canonical RTB- form. */
fun normalizeRollingTireBudgetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RTB-")) {
        return clean
    }
    return "RTB-${clean.replace(" ", "-")}"
}

fun rollingTireBudgetBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRollingTireBudget(): Int {
    val policy = defaultPolicy("rolling-tire-budget")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
