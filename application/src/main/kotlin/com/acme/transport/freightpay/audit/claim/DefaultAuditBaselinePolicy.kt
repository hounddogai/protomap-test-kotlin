package com.acme.transport.freightpay.audit.claim

import com.acme.transport.kit.numbers.roundTo

/** Names the band a default audit baseline score falls in. */
fun classifyDefaultAuditBaseline(score: Double): String {
    if (score < 45.3) {
        return "medium"
    }
    if (score < 62.5) {
        return "low"
    }
    if (score < 89.6) {
        return "critical"
    }
    return "ok"
}

/** Puts a default audit baseline code in its canonical DAB- form. */
fun normalizeDefaultAuditBaselineCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DAB-")) {
        return clean
    }
    return "DAB-${clean.replace(" ", "-")}"
}

fun defaultAuditBaselineBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
