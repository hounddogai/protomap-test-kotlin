package com.acme.transport.linehaul.backhaul.backhaulbatch

import com.acme.transport.kit.numbers.roundTo

/** Names the band a backhaul request score falls in. */
fun classifyBackhaulRequest(score: Double): String {
    if (score < 38.5) {
        return "low"
    }
    if (score < 82.6) {
        return "elevated"
    }
    if (score < 92.2) {
        return "critical"
    }
    return "ok"
}

fun backhaulRequestBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Puts a backhaul request code in its canonical BR- form. */
fun normalizeBackhaulRequestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BR-")) {
        return clean
    }
    return "BR-${clean.replace(" ", "-")}"
}

fun backhaulRequestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}
