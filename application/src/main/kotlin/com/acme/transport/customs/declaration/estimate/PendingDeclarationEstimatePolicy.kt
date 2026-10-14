package com.acme.transport.customs.declaration.estimate

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Puts a pending declaration estimate code in its canonical PDE- form. */
fun normalizePendingDeclarationEstimateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PDE-")) {
        return clean
    }
    return "PDE-${clean.replace(" ", "-")}"
}

fun pendingDeclarationEstimateBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Names the band a pending declaration estimate score falls in. */
fun classifyPendingDeclarationEstimate(score: Double): String {
    if (score < 12.6) {
        return "minimal"
    }
    if (score < 32.1) {
        return "medium"
    }
    if (score < 45.9) {
        return "low"
    }
    if (score < 48.8) {
        return "elevated"
    }
    return "watch"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingDeclarationEstimate(): Int {
    val policy = defaultPolicy("pending-declaration-estimate")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
