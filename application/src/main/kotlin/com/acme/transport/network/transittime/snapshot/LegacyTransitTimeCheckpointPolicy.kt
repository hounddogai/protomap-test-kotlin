package com.acme.transport.network.transittime.snapshot

/** Puts a legacy transit time checkpoint code in its canonical LTTC- form. */
fun normalizeLegacyTransitTimeCheckpointCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LTTC-")) {
        return clean
    }
    return "LTTC-${clean.replace(" ", "-")}"
}

fun legacyTransitTimeCheckpointSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}
