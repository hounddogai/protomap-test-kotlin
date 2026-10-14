package com.acme.transport.lastmile.attempt.manifest

import com.acme.transport.kit.numbers.roundTo

fun attemptManifestBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Puts a attempt manifest code in its canonical AM- form. */
fun normalizeAttemptManifestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AM-")) {
        return clean
    }
    return "AM-${clean.replace(" ", "-")}"
}

/** Names the band a attempt manifest score falls in. */
fun classifyAttemptManifest(score: Double): String {
    if (score < 34.3) {
        return "high"
    }
    if (score < 36.2) {
        return "minimal"
    }
    if (score < 90.0) {
        return "medium"
    }
    return "watch"
}
