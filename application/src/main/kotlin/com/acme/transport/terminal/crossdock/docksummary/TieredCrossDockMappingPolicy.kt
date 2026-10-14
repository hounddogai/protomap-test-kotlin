package com.acme.transport.terminal.crossdock.docksummary

/** Puts a tiered cross dock mapping code in its canonical TCDM- form. */
fun normalizeTieredCrossDockMappingCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TCDM-")) {
        return clean
    }
    return "TCDM-${clean.replace(" ", "-")}"
}

fun tieredCrossDockMappingSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

/** Names the band a tiered cross dock mapping score falls in. */
fun classifyTieredCrossDockMapping(score: Double): String {
    if (score < 34.2) {
        return "low"
    }
    if (score < 43.5) {
        return "high"
    }
    if (score < 43.9) {
        return "elevated"
    }
    if (score < 69.8) {
        return "critical"
    }
    return "medium"
}
