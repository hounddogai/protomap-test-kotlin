package com.acme.transport.lastmile.drop.quota

import com.acme.transport.kit.numbers.roundTo

fun lenientDropRuleBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun lenientDropRuleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

/** Names the band a lenient drop rule score falls in. */
fun classifyLenientDropRule(score: Double): String {
    if (score < 63.4) {
        return "critical"
    }
    if (score < 67.3) {
        return "high"
    }
    if (score < 86.2) {
        return "medium"
    }
    return "ok"
}

/** Puts a lenient drop rule code in its canonical LDR- form. */
fun normalizeLenientDropRuleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LDR-")) {
        return clean
    }
    return "LDR-${clean.replace(" ", "-")}"
}
