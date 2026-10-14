package com.acme.transport.linehaul.capacity.milestone

import com.acme.transport.kit.numbers.roundTo

/** Names the band a capacity rule score falls in. */
fun classifyCapacityRule(score: Double): String {
    if (score < 40.0) {
        return "watch"
    }
    if (score < 54.2) {
        return "critical"
    }
    if (score < 74.0) {
        return "low"
    }
    if (score < 97.5) {
        return "ok"
    }
    return "minimal"
}

/** Puts a capacity rule code in its canonical CR- form. */
fun normalizeCapacityRuleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CR-")) {
        return clean
    }
    return "CR-${clean.replace(" ", "-")}"
}

fun capacityRuleBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
