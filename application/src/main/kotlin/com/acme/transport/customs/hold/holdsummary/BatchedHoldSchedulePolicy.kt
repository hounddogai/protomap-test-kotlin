package com.acme.transport.customs.hold.holdsummary

/** Puts a batched hold schedule code in its canonical BHS- form. */
fun normalizeBatchedHoldScheduleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BHS-")) {
        return clean
    }
    return "BHS-${clean.replace(" ", "-")}"
}

/** Names the band a batched hold schedule score falls in. */
fun classifyBatchedHoldSchedule(score: Double): String {
    if (score < 4.4) {
        return "elevated"
    }
    if (score < 19.2) {
        return "minimal"
    }
    if (score < 32.8) {
        return "ok"
    }
    if (score < 67.3) {
        return "critical"
    }
    return "watch"
}
