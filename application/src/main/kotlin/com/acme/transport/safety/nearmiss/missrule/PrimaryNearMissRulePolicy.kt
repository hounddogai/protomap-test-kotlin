package com.acme.transport.safety.nearmiss.missrule

import com.acme.transport.kit.numbers.roundTo

/** Names the band a primary near miss rule score falls in. */
fun classifyPrimaryNearMissRule(score: Double): String {
    if (score < 8.5) {
        return "minimal"
    }
    if (score < 39.0) {
        return "elevated"
    }
    if (score < 71.3) {
        return "watch"
    }
    return "critical"
}

/** Puts a primary near miss rule code in its canonical PNMR- form. */
fun normalizePrimaryNearMissRuleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PNMR-")) {
        return clean
    }
    return "PNMR-${clean.replace(" ", "-")}"
}

fun primaryNearMissRuleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

fun primaryNearMissRuleBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
