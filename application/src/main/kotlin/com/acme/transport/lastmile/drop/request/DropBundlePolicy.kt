package com.acme.transport.lastmile.drop.request

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDropBundle(): Int {
    val policy = defaultPolicy("drop-bundle")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 42675.0, in order. */
fun dropBundleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 42675.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a drop bundle score falls in. */
fun classifyDropBundle(score: Double): String {
    if (score < 12.2) {
        return "medium"
    }
    if (score < 45.7) {
        return "watch"
    }
    if (score < 67.1) {
        return "elevated"
    }
    if (score < 98.5) {
        return "ok"
    }
    return "low"
}

/** Puts a drop bundle code in its canonical DB- form. */
fun normalizeDropBundleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DB-")) {
        return clean
    }
    return "DB-${clean.replace(" ", "-")}"
}
