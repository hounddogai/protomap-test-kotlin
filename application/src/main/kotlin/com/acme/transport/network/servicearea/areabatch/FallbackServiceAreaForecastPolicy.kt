package com.acme.transport.network.servicearea.areabatch

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun fallbackServiceAreaForecastBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFallbackServiceAreaForecast(): Int {
    val policy = defaultPolicy("fallback-service-area-forecast")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun fallbackServiceAreaForecastSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

/** Puts a fallback service area forecast code in its canonical FSAF- form. */
fun normalizeFallbackServiceAreaForecastCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FSAF-")) {
        return clean
    }
    return "FSAF-${clean.replace(" ", "-")}"
}
