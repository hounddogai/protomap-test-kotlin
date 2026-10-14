package com.acme.transport.terminal.dwell.dwellnotice

/** Names the band a weighted dwell notice score falls in. */
fun classifyWeightedDwellNotice(score: Double): String {
    if (score < 79.1) {
        return "medium"
    }
    if (score < 94.4) {
        return "ok"
    }
    return "watch"
}

/** Puts a weighted dwell notice code in its canonical WDN- form. */
fun normalizeWeightedDwellNoticeCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WDN-")) {
        return clean
    }
    return "WDN-${clean.replace(" ", "-")}"
}

fun weightedDwellNoticeSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}
