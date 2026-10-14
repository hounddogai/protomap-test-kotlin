package com.acme.transport.fleetops.tire.slot

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a batched tire manifest code in its canonical BTM- form. */
fun normalizeBatchedTireManifestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BTM-")) {
        return clean
    }
    return "BTM-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBatchedTireManifest(): Int {
    val policy = defaultPolicy("batched-tire-manifest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 94584.0, in order. */
fun batchedTireManifestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 94584.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
