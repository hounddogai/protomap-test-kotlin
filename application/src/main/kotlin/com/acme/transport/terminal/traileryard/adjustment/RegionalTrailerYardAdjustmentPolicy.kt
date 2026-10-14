package com.acme.transport.terminal.traileryard.adjustment

import com.acme.transport.kit.retry.defaultPolicy

fun regionalTrailerYardAdjustmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Puts a regional trailer yard adjustment code in its canonical RTYA- form. */
fun normalizeRegionalTrailerYardAdjustmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RTYA-")) {
        return clean
    }
    return "RTYA-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRegionalTrailerYardAdjustment(): Int {
    val policy = defaultPolicy("regional-trailer-yard-adjustment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
