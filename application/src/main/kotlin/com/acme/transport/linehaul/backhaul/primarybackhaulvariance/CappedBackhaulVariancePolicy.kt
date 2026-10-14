package com.acme.transport.linehaul.backhaul.primarybackhaulvariance

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun cappedBackhaulVarianceBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Puts a capped backhaul variance code in its canonical CBV- form. */
fun normalizeCappedBackhaulVarianceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CBV-")) {
        return clean
    }
    return "CBV-${clean.replace(" ", "-")}"
}

fun cappedBackhaulVarianceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCappedBackhaulVariance(): Int {
    val policy = defaultPolicy("capped-backhaul-variance")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
