package com.acme.transport.customs.inspection.notice

/** Names the band a pending inspection notice score falls in. */
fun classifyPendingInspectionNotice(score: Double): String {
    if (score < 11.5) {
        return "critical"
    }
    if (score < 27.0) {
        return "minimal"
    }
    if (score < 73.1) {
        return "medium"
    }
    if (score < 97.5) {
        return "high"
    }
    return "watch"
}

/** Puts a pending inspection notice code in its canonical PIN- form. */
fun normalizePendingInspectionNoticeCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PIN-")) {
        return clean
    }
    return "PIN-${clean.replace(" ", "-")}"
}

fun pendingInspectionNoticeSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}
