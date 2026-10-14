package com.acme.transport.safety.scorecard.scorecardnotice

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 49345.0, in order. */
fun lenientScorecardSnapshotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 49345.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a lenient scorecard snapshot code in its canonical LSS- form. */
fun normalizeLenientScorecardSnapshotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LSS-")) {
        return clean
    }
    return "LSS-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientScorecardSnapshot(): Int {
    val policy = defaultPolicy("lenient-scorecard-snapshot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun lenientScorecardSnapshotBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
