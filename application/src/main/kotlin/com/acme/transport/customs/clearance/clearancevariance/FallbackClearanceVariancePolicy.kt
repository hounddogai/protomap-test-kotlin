package com.acme.transport.customs.clearance.clearancevariance

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Puts a fallback clearance variance code in its canonical FCV- form. */
fun normalizeFallbackClearanceVarianceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FCV-")) {
        return clean
    }
    return "FCV-${clean.replace(" ", "-")}"
}

fun fallbackClearanceVarianceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

fun fallbackClearanceVarianceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.87
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFallbackClearanceVariance(): Int {
    val policy = defaultPolicy("fallback-clearance-variance")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
