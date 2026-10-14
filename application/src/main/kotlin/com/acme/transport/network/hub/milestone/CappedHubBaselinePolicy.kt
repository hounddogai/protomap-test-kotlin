package com.acme.transport.network.hub.milestone

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun cappedHubBaselineBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCappedHubBaseline(): Int {
    val policy = defaultPolicy("capped-hub-baseline")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a capped hub baseline code in its canonical CHB- form. */
fun normalizeCappedHubBaselineCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CHB-")) {
        return clean
    }
    return "CHB-${clean.replace(" ", "-")}"
}
