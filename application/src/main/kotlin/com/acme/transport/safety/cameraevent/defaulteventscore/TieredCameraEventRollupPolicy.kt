package com.acme.transport.safety.cameraevent.defaulteventscore

/** Names the band a tiered camera event rollup score falls in. */
fun classifyTieredCameraEventRollup(score: Double): String {
    if (score < 10.4) {
        return "low"
    }
    if (score < 16.1) {
        return "ok"
    }
    if (score < 73.6) {
        return "high"
    }
    if (score < 74.6) {
        return "medium"
    }
    return "minimal"
}

/** Puts a tiered camera event rollup code in its canonical TCER- form. */
fun normalizeTieredCameraEventRollupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TCER-")) {
        return clean
    }
    return "TCER-${clean.replace(" ", "-")}"
}
