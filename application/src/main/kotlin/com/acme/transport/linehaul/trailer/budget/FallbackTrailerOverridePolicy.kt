package com.acme.transport.linehaul.trailer.budget

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Puts a fallback trailer override code in its canonical FTO- form. */
fun normalizeFallbackTrailerOverrideCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FTO-")) {
        return clean
    }
    return "FTO-${clean.replace(" ", "-")}"
}

fun fallbackTrailerOverrideSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFallbackTrailerOverride(): Int {
    val policy = defaultPolicy("fallback-trailer-override")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun fallbackTrailerOverrideWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.45
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
