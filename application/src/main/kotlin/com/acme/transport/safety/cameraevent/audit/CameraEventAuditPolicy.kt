package com.acme.transport.safety.cameraevent.audit

import com.acme.transport.kit.numbers.roundTo

fun cameraEventAuditBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a camera event audit score falls in. */
fun classifyCameraEventAudit(score: Double): String {
    if (score < 41.4) {
        return "watch"
    }
    if (score < 91.0) {
        return "critical"
    }
    return "elevated"
}

/** Counts how many amounts fit in a budget of 85716.0, in order. */
fun cameraEventAuditBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 85716.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a camera event audit code in its canonical CEA- form. */
fun normalizeCameraEventAuditCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CEA-")) {
        return clean
    }
    return "CEA-${clean.replace(" ", "-")}"
}
