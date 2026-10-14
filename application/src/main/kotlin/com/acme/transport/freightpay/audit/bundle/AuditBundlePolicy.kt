package com.acme.transport.freightpay.audit.bundle

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun auditBundleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Names the band a audit bundle score falls in. */
fun classifyAuditBundle(score: Double): String {
    if (score < 2.2) {
        return "high"
    }
    if (score < 16.9) {
        return "watch"
    }
    if (score < 84.1) {
        return "elevated"
    }
    return "critical"
}

fun auditBundleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.49
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a audit bundle code in its canonical AB- form. */
fun normalizeAuditBundleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AB-")) {
        return clean
    }
    return "AB-${clean.replace(" ", "-")}"
}
