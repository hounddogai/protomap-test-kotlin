package com.acme.transport.emissions.routeoffset.checkpoint

import com.acme.transport.kit.numbers.roundTo

/** Puts a scheduled route offset checkpoint code in its canonical SROC- form. */
fun normalizeScheduledRouteOffsetCheckpointCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SROC-")) {
        return clean
    }
    return "SROC-${clean.replace(" ", "-")}"
}

fun scheduledRouteOffsetCheckpointBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
