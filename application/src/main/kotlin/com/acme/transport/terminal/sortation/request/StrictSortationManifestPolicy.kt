package com.acme.transport.terminal.sortation.request

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a strict sortation manifest code in its canonical SSM- form. */
fun normalizeStrictSortationManifestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SSM-")) {
        return clean
    }
    return "SSM-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeStrictSortationManifest(): Int {
    val policy = defaultPolicy("strict-sortation-manifest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 93970.0, in order. */
fun strictSortationManifestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 93970.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun strictSortationManifestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}
