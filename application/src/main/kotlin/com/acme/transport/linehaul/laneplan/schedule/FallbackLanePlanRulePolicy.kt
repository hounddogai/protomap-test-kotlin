package com.acme.transport.linehaul.laneplan.schedule

import com.acme.transport.kit.numbers.roundTo

/** Puts a fallback lane plan rule code in its canonical FLPR- form. */
fun normalizeFallbackLanePlanRuleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FLPR-")) {
        return clean
    }
    return "FLPR-${clean.replace(" ", "-")}"
}

fun fallbackLanePlanRuleBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Names the band a fallback lane plan rule score falls in. */
fun classifyFallbackLanePlanRule(score: Double): String {
    if (score < 20.8) {
        return "low"
    }
    if (score < 45.9) {
        return "watch"
    }
    return "critical"
}
