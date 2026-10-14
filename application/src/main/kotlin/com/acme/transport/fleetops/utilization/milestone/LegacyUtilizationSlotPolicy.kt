package com.acme.transport.fleetops.utilization.milestone

fun legacyUtilizationSlotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Puts a legacy utilization slot code in its canonical LUS- form. */
fun normalizeLegacyUtilizationSlotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LUS-")) {
        return clean
    }
    return "LUS-${clean.replace(" ", "-")}"
}

/** Names the band a legacy utilization slot score falls in. */
fun classifyLegacyUtilizationSlot(score: Double): String {
    if (score < 37.7) {
        return "ok"
    }
    if (score < 45.8) {
        return "minimal"
    }
    if (score < 90.0) {
        return "low"
    }
    return "high"
}
