package com.acme.transport.fleetops.utilization.defaultutilizationsummary

/** Puts a pending utilization summary code in its canonical PUS- form. */
fun normalizePendingUtilizationSummaryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PUS-")) {
        return clean
    }
    return "PUS-${clean.replace(" ", "-")}"
}

/** Names the band a pending utilization summary score falls in. */
fun classifyPendingUtilizationSummary(score: Double): String {
    if (score < 20.2) {
        return "critical"
    }
    if (score < 88.5) {
        return "watch"
    }
    return "elevated"
}

fun pendingUtilizationSummarySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}
