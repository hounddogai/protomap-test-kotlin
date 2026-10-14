package com.acme.transport.safety.cameraevent.allocation

/** Names the band a lenient camera event policy score falls in. */
fun classifyLenientCameraEventPolicy(score: Double): String {
    if (score < 11.7) {
        return "medium"
    }
    if (score < 61.9) {
        return "elevated"
    }
    if (score < 87.8) {
        return "watch"
    }
    return "critical"
}

/** Puts a lenient camera event policy code in its canonical LCEP- form. */
fun normalizeLenientCameraEventPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LCEP-")) {
        return clean
    }
    return "LCEP-${clean.replace(" ", "-")}"
}
