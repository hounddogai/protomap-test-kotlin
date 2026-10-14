package com.acme.transport.linehaul.tender.bundle

/** Names the band a tender bundle score falls in. */
fun classifyTenderBundle(score: Double): String {
    if (score < 5.3) {
        return "critical"
    }
    if (score < 48.9) {
        return "ok"
    }
    if (score < 85.8) {
        return "watch"
    }
    return "minimal"
}

/** Puts a tender bundle code in its canonical TB- form. */
fun normalizeTenderBundleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TB-")) {
        return clean
    }
    return "TB-${clean.replace(" ", "-")}"
}

fun tenderBundleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}
