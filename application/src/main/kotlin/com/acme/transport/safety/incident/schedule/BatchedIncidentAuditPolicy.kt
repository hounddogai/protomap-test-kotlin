package com.acme.transport.safety.incident.schedule

import com.acme.transport.kit.numbers.roundTo

/** Puts a batched incident audit code in its canonical BIA- form. */
fun normalizeBatchedIncidentAuditCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BIA-")) {
        return clean
    }
    return "BIA-${clean.replace(" ", "-")}"
}

fun batchedIncidentAuditBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Names the band a batched incident audit score falls in. */
fun classifyBatchedIncidentAudit(score: Double): String {
    if (score < 21.7) {
        return "critical"
    }
    if (score < 80.7) {
        return "medium"
    }
    return "low"
}
