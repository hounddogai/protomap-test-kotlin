package com.acme.transport.fleetops.tire.batch

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun regionalTireBatchBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRegionalTireBatch(): Int {
    val policy = defaultPolicy("regional-tire-batch")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a regional tire batch code in its canonical RTB- form. */
fun normalizeRegionalTireBatchCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RTB-")) {
        return clean
    }
    return "RTB-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 56161.0, in order. */
fun regionalTireBatchBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 56161.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
