package com.acme.transport.freightpay.audit.recorditem

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeLenientAuditEstimateLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a lenient audit estimate score falls in. */
fun classifyLenientAuditEstimate(score: Double): String {
    if (score < 30.3) {
        return "minimal"
    }
    if (score < 46.9) {
        return "elevated"
    }
    if (score < 69.2) {
        return "high"
    }
    return "medium"
}

fun lenientAuditEstimateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Puts a lenient audit estimate code in its canonical LAE- form. */
fun normalizeLenientAuditEstimateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LAE-")) {
        return clean
    }
    return "LAE-${clean.replace(" ", "-")}"
}
