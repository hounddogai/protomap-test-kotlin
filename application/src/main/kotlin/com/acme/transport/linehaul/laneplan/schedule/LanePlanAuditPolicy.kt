package com.acme.transport.linehaul.laneplan.schedule

import com.acme.transport.kit.numbers.roundTo

/** Names the band a lane plan audit score falls in. */
fun classifyLanePlanAudit(score: Double): String {
    if (score < 34.6) {
        return "critical"
    }
    if (score < 48.4) {
        return "ok"
    }
    if (score < 59.6) {
        return "medium"
    }
    if (score < 87.5) {
        return "minimal"
    }
    return "watch"
}

/** Puts a lane plan audit code in its canonical LPA- form. */
fun normalizeLanePlanAuditCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LPA-")) {
        return clean
    }
    return "LPA-${clean.replace(" ", "-")}"
}

fun lanePlanAuditBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
