package com.acme.transport.freightpay.audit.snapshot

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a audit snapshot score falls in. */
fun classifyAuditSnapshot(score: Double): String {
    if (score < 63.6) {
        return "minimal"
    }
    if (score < 86.1) {
        return "critical"
    }
    return "elevated"
}

/** Puts a audit snapshot code in its canonical AS- form. */
fun normalizeAuditSnapshotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AS-")) {
        return clean
    }
    return "AS-${clean.replace(" ", "-")}"
}

fun auditSnapshotWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.94
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun auditSnapshotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}
