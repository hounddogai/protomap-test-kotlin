package com.acme.transport.linehaul.backhaul.backhaulaudit

import com.acme.transport.kit.numbers.roundTo

fun backhaulPlanBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Names the band a backhaul plan score falls in. */
fun classifyBackhaulPlan(score: Double): String {
    if (score < 34.3) {
        return "high"
    }
    if (score < 45.0) {
        return "ok"
    }
    if (score < 64.7) {
        return "medium"
    }
    if (score < 93.2) {
        return "minimal"
    }
    return "elevated"
}

/** Puts a backhaul plan code in its canonical BP- form. */
fun normalizeBackhaulPlanCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BP-")) {
        return clean
    }
    return "BP-${clean.replace(" ", "-")}"
}
