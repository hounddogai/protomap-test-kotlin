package com.acme.transport.terminal.traileryard.exception

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTrailerYardPlan(): Int {
    val policy = defaultPolicy("trailer-yard-plan")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a trailer yard plan code in its canonical TYP- form. */
fun normalizeTrailerYardPlanCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TYP-")) {
        return clean
    }
    return "TYP-${clean.replace(" ", "-")}"
}

fun trailerYardPlanBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Counts how many amounts fit in a budget of 89126.0, in order. */
fun trailerYardPlanBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 89126.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
