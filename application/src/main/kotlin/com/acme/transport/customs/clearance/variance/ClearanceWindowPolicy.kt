package com.acme.transport.customs.clearance.variance

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun clearanceWindowBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Counts how many amounts fit in a budget of 71533.0, in order. */
fun clearanceWindowBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 71533.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeClearanceWindow(): Int {
    val policy = defaultPolicy("clearance-window")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a clearance window code in its canonical CW- form. */
fun normalizeClearanceWindowCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CW-")) {
        return clean
    }
    return "CW-${clean.replace(" ", "-")}"
}
