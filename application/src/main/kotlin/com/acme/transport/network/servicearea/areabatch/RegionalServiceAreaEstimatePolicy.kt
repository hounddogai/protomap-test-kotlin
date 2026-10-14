package com.acme.transport.network.servicearea.areabatch

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRegionalServiceAreaEstimate(): Int {
    val policy = defaultPolicy("regional-service-area-estimate")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun regionalServiceAreaEstimateBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Puts a regional service area estimate code in its canonical RSAE- form. */
fun normalizeRegionalServiceAreaEstimateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RSAE-")) {
        return clean
    }
    return "RSAE-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 18993.0, in order. */
fun regionalServiceAreaEstimateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 18993.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
