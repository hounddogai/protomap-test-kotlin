package com.acme.transport.lastmile.attempt.attemptclaim

/** Puts a adaptive attempt manifest code in its canonical AAM- form. */
fun normalizeAdaptiveAttemptManifestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AAM-")) {
        return clean
    }
    return "AAM-${clean.replace(" ", "-")}"
}

fun adaptiveAttemptManifestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

/** Names the band a adaptive attempt manifest score falls in. */
fun classifyAdaptiveAttemptManifest(score: Double): String {
    if (score < 14.9) {
        return "elevated"
    }
    if (score < 85.8) {
        return "medium"
    }
    return "low"
}
