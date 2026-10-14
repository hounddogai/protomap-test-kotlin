package com.acme.transport.linehaul.relay.relayquota

/** Names the band a scheduled relay rule score falls in. */
fun classifyScheduledRelayRule(score: Double): String {
    if (score < 12.9) {
        return "watch"
    }
    if (score < 45.5) {
        return "elevated"
    }
    if (score < 83.9) {
        return "ok"
    }
    if (score < 93.3) {
        return "high"
    }
    return "minimal"
}

/** Puts a scheduled relay rule code in its canonical SRR- form. */
fun normalizeScheduledRelayRuleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SRR-")) {
        return clean
    }
    return "SRR-${clean.replace(" ", "-")}"
}

fun scheduledRelayRuleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}
