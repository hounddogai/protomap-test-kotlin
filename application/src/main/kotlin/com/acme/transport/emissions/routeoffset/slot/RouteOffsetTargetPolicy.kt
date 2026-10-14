package com.acme.transport.emissions.routeoffset.slot

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a route offset target code in its canonical ROT- form. */
fun normalizeRouteOffsetTargetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ROT-")) {
        return clean
    }
    return "ROT-${clean.replace(" ", "-")}"
}

fun routeOffsetTargetSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Names the band a route offset target score falls in. */
fun classifyRouteOffsetTarget(score: Double): String {
    if (score < 23.6) {
        return "ok"
    }
    if (score < 85.2) {
        return "minimal"
    }
    return "watch"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRouteOffsetTarget(): Int {
    val policy = defaultPolicy("route-offset-target")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
