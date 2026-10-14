package com.acme.transport.network.zone.segment

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun fallbackZoneSummaryBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Puts a fallback zone summary code in its canonical FZS- form. */
fun normalizeFallbackZoneSummaryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FZS-")) {
        return clean
    }
    return "FZS-${clean.replace(" ", "-")}"
}

fun fallbackZoneSummarySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFallbackZoneSummary(): Int {
    val policy = defaultPolicy("fallback-zone-summary")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
