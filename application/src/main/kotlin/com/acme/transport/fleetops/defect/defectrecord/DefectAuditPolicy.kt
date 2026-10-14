package com.acme.transport.fleetops.defect.defectrecord

import com.acme.transport.kit.numbers.roundTo

fun defectAuditSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Names the band a defect audit score falls in. */
fun classifyDefectAudit(score: Double): String {
    if (score < 31.6) {
        return "minimal"
    }
    if (score < 53.2) {
        return "elevated"
    }
    return "critical"
}

fun defectAuditBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Puts a defect audit code in its canonical DA- form. */
fun normalizeDefectAuditCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DA-")) {
        return clean
    }
    return "DA-${clean.replace(" ", "-")}"
}
