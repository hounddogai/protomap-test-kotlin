package com.acme.transport.terminal.traileryard.template

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a trailer yard template code in its canonical TYT- form. */
fun normalizeTrailerYardTemplateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TYT-")) {
        return clean
    }
    return "TYT-${clean.replace(" ", "-")}"
}

fun trailerYardTemplateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTrailerYardTemplate(): Int {
    val policy = defaultPolicy("trailer-yard-template")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
