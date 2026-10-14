package com.acme.transport.safety.incident.incidentsnapshot

/** Puts a weighted incident snapshot code in its canonical WIS- form. */
fun normalizeWeightedIncidentSnapshotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WIS-")) {
        return clean
    }
    return "WIS-${clean.replace(" ", "-")}"
}

/** Names the band a weighted incident snapshot score falls in. */
fun classifyWeightedIncidentSnapshot(score: Double): String {
    if (score < 61.2) {
        return "elevated"
    }
    if (score < 68.4) {
        return "watch"
    }
    return "high"
}

fun weightedIncidentSnapshotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}
