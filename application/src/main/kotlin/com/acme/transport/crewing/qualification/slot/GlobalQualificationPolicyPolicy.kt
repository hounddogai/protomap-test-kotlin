package com.acme.transport.crewing.qualification.slot

/** Puts a global qualification policy code in its canonical GQP- form. */
fun normalizeGlobalQualificationPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GQP-")) {
        return clean
    }
    return "GQP-${clean.replace(" ", "-")}"
}

/** Names the band a global qualification policy score falls in. */
fun classifyGlobalQualificationPolicy(score: Double): String {
    if (score < 12.0) {
        return "watch"
    }
    if (score < 69.6) {
        return "ok"
    }
    if (score < 87.1) {
        return "minimal"
    }
    return "low"
}

fun globalQualificationPolicySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}
